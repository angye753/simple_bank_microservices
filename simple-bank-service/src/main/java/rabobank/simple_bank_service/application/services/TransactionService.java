package rabobank.simple_bank_service.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rabobank.simple_bank_service.application.auth.SecurityUtils;
import rabobank.simple_bank_service.application.out.messaging.KafkaEventPublisher;
import rabobank.simple_bank_service.domain.model.LedgerEventDTO;
import rabobank.simple_bank_service.domain.model.TransactionDTO;
import rabobank.simple_bank_service.application.in.TransactionPort;
import rabobank.simple_bank_service.application.mappers.AccountMapper;
import rabobank.simple_bank_service.application.mappers.LedgerMapper;
import rabobank.simple_bank_service.domain.model.OutboxEvent;
import rabobank.simple_bank_service.infrastructure.entities.*;
import rabobank.simple_bank_service.infrastructure.idempotency.IdempotencyService;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.infrastructure.repositories.LedgerRepository;
import rabobank.simple_bank_service.infrastructure.repositories.OutboxRepository;
import rabobank.simple_bank_service.infrastructure.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService implements TransactionPort {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepo;
    private final OutboxRepository outboxRepo;
    private final IdempotencyService idempotencyService;
    private final FeeStrategyFactory feeStrategyFactory;
    private final ObjectMapper objectMapper;
    private final LedgerMapper ledgerMapper;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Transactional
    public Long transfer(String idempotencyKey,
                         String fromAccountId,
                         String toAccountId,
                         BigDecimal amount) throws Exception {

        handleIdempotency(idempotencyKey);
        String currentUsername = SecurityUtils.getCurrentUsername();
        log.info("Creating a transfer for user {} ", currentUsername);

        Account from = getAccount(fromAccountId);
        if (!from.getAppUser().getUsername().equals(currentUsername)) {
            log.error("User {} not authorized to transfer from this account", currentUsername);
            throw new AccessDeniedException("You are not authorized to transfer from this account");
        }

        Account to = getAccount(toAccountId);

        BigDecimal fee = calculateFee(from, amount);
        log.info("Fee applied on this transaction {}", fee);
        BigDecimal totalDebited = amount.add(fee);
        ensureSufficientFunds(from, totalDebited);

        Transaction tx = createPendingTransaction(idempotencyKey, from, to, amount);

        createLedgerEntries(from, to, tx, amount, fee);

        updateBalances(from, to, totalDebited, amount);

        finalizeTransaction(tx);
        publishOutboxEvent(tx);

        return tx.getId();
    }


    private void handleIdempotency(String idempotencyKey) {
        if (idempotencyKey == null) return;
        transactionRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(tx -> {
                    throw new IllegalStateException("Duplicate transaction");
                });

        boolean ok = idempotencyService.checkAndMark(idempotencyKey);
        if (!ok) throw new IllegalStateException("Duplicate idempotency key detected");
    }

    private Account getAccount(String id) {
        return accountRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    private BigDecimal calculateFee(Account from, BigDecimal amount) {
        FeeStrategy strategy = feeStrategyFactory.getStrategy(from.getCard().getCardType());
        return strategy.fee(amount);
    }

    private void ensureSufficientFunds(Account from, BigDecimal totalDebited) {
        if (from.getBalance().compareTo(totalDebited) < 0) {
            log.info("Insufficient funds for account {}", from.getId());
            throw new IllegalStateException("Insufficient funds for account " + from.getId());
        }
    }

    private Transaction createPendingTransaction(String idempotencyKey,
                                                 Account from,
                                                 Account to,
                                                 BigDecimal amount) {
        log.info("Create pending transaction");
        Transaction tx = Transaction.builder()
                .account(from)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .state(TransactionState.PENDING)
                .idempotencyKey(idempotencyKey)
                .sender(from)
                .recipient(to)
                .dateTime(LocalDateTime.now())
                .build();
        return transactionRepository.save(tx);
    }

    private void createLedgerEntries(Account from, Account to,
                                     Transaction tx, BigDecimal amount, BigDecimal fee) {

        BigDecimal totalDebited = amount.add(fee);

        Ledger debit = Ledger.builder()
                .owner(from)
                .amount(totalDebited.negate())
                .currency(from.getCurrency())
                .transactionId(tx.getId())
                .createdAt(Instant.now())
                .build();

        Ledger creditTo = Ledger.builder()
                .owner(to)
                .amount(amount)
                .currency(from.getCurrency())
                .transactionId(tx.getId())
                .createdAt(Instant.now())
                .build();

        Ledger creditFee = Ledger.builder()
                .owner(from)
                .amount(fee)
                .currency(from.getCurrency())
                .transactionId(tx.getId())
                .createdAt(Instant.now())
                .build();

        validateLedgerBalance(debit, creditTo, creditFee);
        ledgerRepo.saveAll(List.of(debit, creditTo, creditFee));
        sendEvent(debit, creditTo, creditFee);
    }

    private void sendEvent(Ledger debit, Ledger creditTo, Ledger creditFee) {
        log.info("Sending event to synchronize read tables");
        LedgerEventDTO ledgerEventDTO = LedgerEventDTO.builder().eventType("LedgerRegister")
                .ledgerDTO(List.of(ledgerMapper.toDTO(debit), ledgerMapper.toDTO(creditTo), ledgerMapper.toDTO(creditFee)))
                .build();
        kafkaEventPublisher.publish(ledgerEventDTO);
    }

    private void validateLedgerBalance(Ledger... entries) {
        BigDecimal sum = Arrays.stream(entries)
                .map(Ledger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalStateException("Ledger entries do not balance (sum != 0)");
    }

    private void updateBalances(Account from, Account to, BigDecimal totalDebited, BigDecimal creditedAmount) {
        from.debit(totalDebited);
        to.credit(creditedAmount);
        accountRepo.saveAll(List.of(from, to));
    }

    private void finalizeTransaction(Transaction tx) {
        tx.setState(TransactionState.POSTED);
        transactionRepository.save(tx);
    }

    private void publishOutboxEvent(Transaction tx) throws Exception {

        TransactionDTO transactionDTO = TransactionDTO.builder()
                .id(tx.getId())
                .senderAccountId(tx.getSender().getId())
                .recipientAccountId(tx.getRecipient().getId())
                .amount(tx.getAmount())
                .type(tx.getType().name())
                .state(tx.getState().name())
                .build();

        OutboxEvent ev = OutboxEvent.builder()
                .aggregateType("Transaction")
                .aggregateId(String.valueOf(transactionDTO.getId()))
                .type("TransactionPosted")
                .payload(objectMapper.writeValueAsString(transactionDTO))
                .published(false)
                .createdAt(Instant.now())
                .build();
        outboxRepo.save(ev);
    }

    @Transactional
    public Long withdraw(String idempotencyKey,
                         String accountId,
                         BigDecimal amount) throws Exception {

        handleIdempotency(idempotencyKey);

        Account account = getAccount(accountId);
        ensureSufficientFunds(account, amount);

        Transaction tx = createPendingTransaction(idempotencyKey, account, amount);

        createLedgerEntry(account, tx, amount);

        updateAccountBalance(account, amount);

        finalizeTransaction(tx);

        publishOutboxEvent(tx);
        return tx.getId();
    }

    private Transaction createPendingTransaction(String idempotencyKey, Account account, BigDecimal amount) {
        Transaction tx = Transaction.builder()
                .account(account)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .state(TransactionState.PENDING)
                .idempotencyKey(idempotencyKey)
                .dateTime(LocalDateTime.now())
                .build();
        return transactionRepository.save(tx);
    }

    private void createLedgerEntry(Account account, Transaction tx, BigDecimal amount) {
        Ledger withdrawEntry = Ledger.builder()
                .owner(account)
                .amount(amount.negate()) // debit (negative)
                .currency(account.getCurrency())
                .transactionId(tx.getId())
                .createdAt(Instant.now())
                .build();
        ledgerRepo.save(withdrawEntry);
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        account.debit(amount);
        accountRepo.save(account);
    }

}

