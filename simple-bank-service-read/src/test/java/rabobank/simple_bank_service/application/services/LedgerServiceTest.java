package rabobank.simple_bank_service.application.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rabobank.simple_bank_service.application.mappers.LedgerMapper;
import rabobank.simple_bank_service.domain.exceptions.AccountNotFound;
import rabobank.simple_bank_service.domain.exceptions.InconsistentAmountOfLedger;
import rabobank.simple_bank_service.domain.model.AccountDTO;
import rabobank.simple_bank_service.domain.model.LedgerDTO;
import rabobank.simple_bank_service.domain.model.LedgerEventDTO;
import rabobank.simple_bank_service.infrastructure.entities.Account;
import rabobank.simple_bank_service.infrastructure.entities.Ledger;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.infrastructure.repositories.LedgerRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private LedgerMapper ledgerMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private Account debitAccount;
    private AccountDTO debitAccountDto;
    private Account creditAccount;
    private AccountDTO creditAccountDto;

    @BeforeEach
    void setup() {
        debitAccount = new Account();
        debitAccount.setId("A1");
        debitAccount.setBalance(BigDecimal.valueOf(1000));

        creditAccount = new Account();
        creditAccount.setId("A2");
        creditAccount.setBalance(BigDecimal.valueOf(500));

        debitAccountDto = new AccountDTO();
        debitAccountDto.setId("A1");
        debitAccountDto.setBalance(BigDecimal.valueOf(1000));

        creditAccount = new Account();
        creditAccount.setId("A2");
        creditAccount.setBalance(BigDecimal.valueOf(500));

        creditAccountDto = new AccountDTO();
        creditAccountDto.setId("A2");
        creditAccountDto.setBalance(BigDecimal.valueOf(500));
    }

    @Test
    void shouldProcessLedgerRegisterEventAndUpdateBalances() {
        // Arrange
        Ledger debitLedger = Ledger.builder().id(1L).amount(BigDecimal.valueOf(-110)).build();
        Ledger creditLedger = Ledger.builder().id(2L).amount(BigDecimal.valueOf(100)).build();
        Ledger feeLedger = Ledger.builder().id(3L).amount(BigDecimal.valueOf(10)).build();

        LedgerDTO debitDTO = LedgerDTO.builder().id(1L).owner(debitAccountDto).amount(BigDecimal.valueOf(-110)).build();
        LedgerDTO creditDTO = LedgerDTO.builder().id(2L).owner(creditAccountDto).amount(BigDecimal.valueOf(100)).build();
        LedgerDTO feeDTO = LedgerDTO.builder().id(3L).owner(debitAccountDto).amount(BigDecimal.valueOf(10)).build();

        LedgerEventDTO event = LedgerEventDTO.builder()
                .eventType("LedgerRegister")
                .ledgerDTO(List.of(debitDTO, creditDTO, feeDTO))
                .build();

        when(ledgerMapper.toEntity(debitDTO)).thenReturn(debitLedger);
        when(ledgerMapper.toEntity(creditDTO)).thenReturn(creditLedger);
        when(ledgerMapper.toEntity(feeDTO)).thenReturn(feeLedger);

        when(ledgerRepository.existsById(any())).thenReturn(false);
        when(accountRepository.findById("A1")).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById("A2")).thenReturn(Optional.of(creditAccount));

        // Act
        ledgerService.processLedgerEvents(event);

        // Assert
        verify(ledgerRepository).saveAll(anyList());
        verify(accountRepository, times(2)).save(any(Account.class));
        assertEquals(BigDecimal.valueOf(890), debitAccount.getBalance());
        assertEquals(BigDecimal.valueOf(610), creditAccount.getBalance());
    }

    @Test
    void shouldSkipWhenLedgersAlreadyExist() {
        // Arrange
        LedgerDTO dto = LedgerDTO.builder().id(1L).owner(debitAccountDto).build();
        LedgerEventDTO event = LedgerEventDTO.builder()
                .eventType("LedgerRegister")
                .ledgerDTO(List.of(dto, dto, dto))
                .build();

        when(ledgerMapper.toEntity(any())).thenReturn(new Ledger());
        when(ledgerRepository.existsById(any())).thenReturn(true);

        // Act
        ledgerService.processLedgerEvents(event);

        // Assert
        verify(ledgerRepository, never()).saveAll(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenLedgerCountIsInconsistent() {
        // Arrange
        LedgerDTO dto = LedgerDTO.builder().id(1L).owner(debitAccountDto).build();
        LedgerEventDTO event = LedgerEventDTO.builder()
                .eventType("LedgerRegister")
                .ledgerDTO(List.of(dto, dto)) // only 2 ledgers
                .build();

        when(ledgerMapper.toEntity(any())).thenReturn(new Ledger());
        when(ledgerRepository.existsById(any())).thenReturn(false);

        // Act & Assert
        assertThrows(InconsistentAmountOfLedger.class, () -> ledgerService.processLedgerEvents(event));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        // Arrange
        LedgerDTO debitDTO = LedgerDTO.builder().id(1L).owner(debitAccountDto).amount(BigDecimal.valueOf(-100)).build();
        LedgerDTO creditDTO = LedgerDTO.builder().id(2L).owner(creditAccountDto).amount(BigDecimal.valueOf(100)).build();
        LedgerDTO feeDTO = LedgerDTO.builder().id(3L).owner(debitAccountDto).amount(BigDecimal.ZERO).build();

        LedgerEventDTO event = LedgerEventDTO.builder()
                .eventType("LedgerRegister")
                .ledgerDTO(List.of(debitDTO, creditDTO, feeDTO))
                .build();

        when(ledgerMapper.toEntity(any())).thenReturn(new Ledger());
        when(ledgerRepository.existsById(any())).thenReturn(false);
        when(accountRepository.findById("A1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFound.class, () -> ledgerService.processLedgerEvents(event));
    }
}
