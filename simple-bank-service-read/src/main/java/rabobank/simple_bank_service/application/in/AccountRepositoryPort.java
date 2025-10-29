package rabobank.simple_bank_service.application.in;

import rabobank.simple_bank_service.domain.model.AccountDTO;
import rabobank.simple_bank_service.domain.model.BalanceDTO;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface AccountRepositoryPort {

    List<AccountDTO> getAllAccounts() ;
    List<BalanceDTO> getAccountBalance()  throws AccessDeniedException ;
    List<BalanceDTO> getAccountBalance(String userId) throws AccessDeniedException ;
}
