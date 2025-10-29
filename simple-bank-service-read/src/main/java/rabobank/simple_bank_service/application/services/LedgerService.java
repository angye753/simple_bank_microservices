package rabobank.simple_bank_service.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import rabobank.simple_bank_service.domain.model.LedgerEventDTO;
import rabobank.simple_bank_service.domain.exceptions.AccountNotFound;
import rabobank.simple_bank_service.domain.exceptions.InconsistentAmountOfLedger;
import rabobank.simple_bank_service.application.mappers.LedgerMapper;
import rabobank.simple_bank_service.infrastructure.entities.Account;
import rabobank.simple_bank_service.infrastructure.entities.Ledger;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.infrastructure.repositories.LedgerRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMapper ledgerMapper;
    private final AccountRepository accountRepository;

    @KafkaListener(topics = "ledger-event-topic", groupId =  "ledger-event-group")
    public void processLedgerEvents(LedgerEventDTO ledgerEventDTO) {
        log.info("Processing Ledger Event {} {}", ledgerEventDTO.getEventType(), ledgerEventDTO.getLedgerDTO().toString());

        if ("LedgerRegister".equals(ledgerEventDTO.getEventType())) {;
            List<Ledger> ledgers = ledgerEventDTO.getLedgerDTO().stream()
                    .map(ledgerMapper::toEntity)
                    .filter(ledger -> !ledgerRepository.existsById(ledger.getId()))
                    .toList();

            if (ledgers.isEmpty()) {
                log.info("All ledgers already exist, skipping event: {}", ledgerEventDTO.getEventType());
                return;
            }

            log.info("Saving {} new ledgers", ledgers.size());
            ledgerRepository.saveAll(ledgers);

            if (ledgerEventDTO.getLedgerDTO().size() != 3) {
                throw new InconsistentAmountOfLedger("YOU MUST CHECKOUT TO AVOID DATA INCONSISTENT ");
            }

            log.info("update balance");
            var debitEntry = ledgerEventDTO.getLedgerDTO().get(0);
            var creditEntry = ledgerEventDTO.getLedgerDTO().get(1);
            var creditFeeEntry = ledgerEventDTO.getLedgerDTO().get(2);

            Account debitAccount = accountRepository.findById(debitEntry.getOwner().getId())
                    .orElseThrow(() -> new AccountNotFound(
                            "Debit account not found with ID: " + debitEntry.getOwner().getId()
                    ));

            Account creditAccount = accountRepository.findById(creditEntry.getOwner().getId())
                    .orElseThrow(() -> new AccountNotFound(
                            "Credit account not found with ID: " + creditEntry.getOwner().getId()
                    ));

            debitAccount.debit(debitEntry.getAmount().abs());
            creditAccount.credit(creditEntry.getAmount().add(creditFeeEntry.getAmount()));

            accountRepository.save(debitAccount);
            accountRepository.save(creditAccount);
        }
    }
}
