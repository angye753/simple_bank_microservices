package rabobank.simple_bank_service.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rabobank.simple_bank_service.application.auth.SecurityUtils;
import rabobank.simple_bank_service.application.in.AccountRepositoryPort;
import rabobank.simple_bank_service.domain.model.BalanceDTO;
import rabobank.simple_bank_service.domain.exceptions.UserNotFound;
import rabobank.simple_bank_service.application.mappers.AccountMapper;
import rabobank.simple_bank_service.infrastructure.entities.Account;
import rabobank.simple_bank_service.infrastructure.entities.AppUser;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.domain.model.AccountDTO;
import rabobank.simple_bank_service.infrastructure.repositories.AppUserRepository;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements AccountRepositoryPort {

    @Autowired
    private final AccountRepository accountRepository;

    @Autowired
    private final AppUserRepository appUserRepository;

    @Autowired
    private final AccountMapper accountMapper;


    public List<AccountDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream().map(accountMapper::toDTO).toList();
    }

    public List<BalanceDTO> getAccountBalance() throws AccessDeniedException {
        String currentUsername = SecurityUtils.getCurrentUsername();
        AppUser appUser = appUserRepository.findByUsername(currentUsername).orElseThrow(() -> new UserNotFound("User not found: " + currentUsername));
        if (!appUser.getRole().equalsIgnoreCase("admin")) {
            throw new AccessDeniedException("You are not authorized to see the balance of all accounts");
        }
        List<Account> accounts = accountRepository.findAll();
        List<BalanceDTO> balanceDTOS = accounts.stream().map(account -> BalanceDTO.builder()
                .accountNumber(account.getId())
                .ownerAccount(account.getAppUser().getName())
                .balance(account.getBalance()).build()).toList();
        return balanceDTOS;
    }

    public List<BalanceDTO> getAccountBalance(String userId) throws AccessDeniedException {
        String currentUsername = SecurityUtils.getCurrentUsername();
        log.info("get account balance for user {}", currentUsername);
        AppUser appUser = appUserRepository.findById(userId).orElseThrow(() -> new UserNotFound("User not found: " + userId));
        log.info("appUser {}" ,appUser);
        if (!appUser.getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not authorized to see the balance of this account");
        }
        List<Account> accounts = accountRepository.findByAppUser(appUser).stream().toList();
        log.info("accounts {} ", accounts);
        List<BalanceDTO> balanceDTOS = accounts.stream().map(account -> BalanceDTO.builder()
                .accountNumber(account.getId())
                .ownerAccount(account.getAppUser().getName())
                .balance(account.getBalance()).build()).toList();
        return balanceDTOS;
    }
}

