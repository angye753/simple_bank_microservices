package rabobank.simple_bank_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import rabobank.simple_bank_service.application.auth.SecurityUtils;
import rabobank.simple_bank_service.application.mappers.LedgerMapper;
import rabobank.simple_bank_service.application.out.messaging.KafkaEventPublisher;
import rabobank.simple_bank_service.application.services.FeeStrategy;
import rabobank.simple_bank_service.application.services.FeeStrategyFactory;
import rabobank.simple_bank_service.application.services.TransactionService;
import rabobank.simple_bank_service.domain.model.LedgerDTO;
import rabobank.simple_bank_service.infrastructure.entities.*;
import rabobank.simple_bank_service.infrastructure.idempotency.IdempotencyService;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.infrastructure.repositories.LedgerRepository;
import rabobank.simple_bank_service.infrastructure.repositories.OutboxRepository;
import rabobank.simple_bank_service.infrastructure.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock private AccountRepository accountRepo;
    @Mock private TransactionRepository transactionRepo;
    @Mock private LedgerRepository ledgerRepo;
    @Mock private OutboxRepository outboxRepo;
    @Mock private IdempotencyService idempotencyService;
    @Mock private FeeStrategyFactory feeStrategyFactory;
    @Mock private FeeStrategy feeStrategy;
    @Mock private ObjectMapper objectMapper;
    @Mock private LedgerMapper ledgerMapper;
    @Mock private KafkaEventPublisher kafkaPublisher;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;


    @InjectMocks private TransactionService transactionService;

    private Account fromAccount;
    private Account toAccount;
    private AppUser user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new AppUser();
        user.setUsername("john");

        fromAccount = new Account();
        fromAccount.setId("A1");
        fromAccount.setAppUser(user);
        fromAccount.setBalance(BigDecimal.valueOf(1000));
        fromAccount.setCurrency("USD");

        Card card = new Card();
        card.setCardType(CardType.DEBIT);
        fromAccount.setCard(card);

        toAccount = new Account();
        toAccount.setId("A2");
        toAccount.setBalance(BigDecimal.valueOf(500));
        toAccount.setCurrency("USD");

        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("john");
        when(SecurityUtils.getCurrentUsername()).thenReturn("john");
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void transfer_ShouldCreateTransactionAndLedgerEntries() throws Exception {
        String idempotencyKey = "unique-key";
        BigDecimal amount = BigDecimal.valueOf(100);
        when(ledgerMapper.toDTO(any(Ledger.class))).thenReturn(new LedgerDTO());

        when(idempotencyService.checkAndMark(idempotencyKey)).thenReturn(true);
        when(transactionRepo.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountRepo.findById("A1")).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findById("A2")).thenReturn(Optional.of(toAccount));

        when(feeStrategyFactory.getStrategy(any())).thenReturn(feeStrategy);
        when(feeStrategy.fee(amount)).thenReturn(BigDecimal.TEN);

        Transaction savedTx = Transaction.builder()
                .id(1L)
                .account(fromAccount)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .state(TransactionState.PENDING)
                .idempotencyKey(idempotencyKey)
                .sender(fromAccount)
                .recipient(toAccount)
                .dateTime(LocalDateTime.now())
                .build();

        when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTx);

        Long txId = transactionService.transfer(idempotencyKey, "A1", "A2", amount);

        assertEquals(1L, txId);
        verify(accountRepo, times(1)).saveAll(anyList());
        verify(ledgerRepo, times(1)).saveAll(anyList());
        verify(outboxRepo, times(1)).save(any());
        verify(kafkaPublisher, times(1)).publish(any());
    }

    @Test
    void transfer_ShouldThrowIfUserNotOwner() {
        when(idempotencyService.checkAndMark("id-key")).thenReturn(true);
        when(transactionRepo.findByIdempotencyKey("id-key")).thenReturn(Optional.empty());
        when(accountRepo.findById("A1")).thenReturn(Optional.of(fromAccount));
        when(SecurityUtils.getCurrentUsername()).thenReturn("someoneElse");

        assertThrows(AccessDeniedException.class,
                () -> transactionService.transfer("id-key", "A1", "A2", BigDecimal.valueOf(100)));
    }

    @Test
    void transfer_ShouldThrowIfInsufficientFunds() {
        fromAccount.setBalance(BigDecimal.valueOf(50));

        when(idempotencyService.checkAndMark("key")).thenReturn(true);
        when(transactionRepo.findByIdempotencyKey("key")).thenReturn(Optional.empty());
        when(accountRepo.findById("A1")).thenReturn(Optional.of(fromAccount));
        when(accountRepo.findById("A2")).thenReturn(Optional.of(toAccount));

        when(feeStrategyFactory.getStrategy(any())).thenReturn(feeStrategy);
        when(feeStrategy.fee(any())).thenReturn(BigDecimal.TEN);

        assertThrows(IllegalStateException.class,
                () -> transactionService.transfer("key", "A1", "A2", BigDecimal.valueOf(100)));
    }
}
