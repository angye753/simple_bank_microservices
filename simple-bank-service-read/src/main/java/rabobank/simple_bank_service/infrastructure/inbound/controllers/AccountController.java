package rabobank.simple_bank_service.infrastructure.inbound.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import rabobank.simple_bank_service.application.in.AccountRepositoryPort;
import rabobank.simple_bank_service.domain.model.BalanceDTO;

import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepositoryPort accountRepositoryPort;

    @GetMapping
    public Object allAccounts() {
        return accountRepositoryPort.getAllAccounts();
    }

    @GetMapping("/allBalances")
    public List<BalanceDTO> getAccountBalance() throws AccessDeniedException {
        return accountRepositoryPort.getAccountBalance();
    }

    @GetMapping("/myBalance")
    public List<BalanceDTO> getMyBalance(@RequestParam String userId) throws AccessDeniedException {
        return accountRepositoryPort.getAccountBalance(userId);
    }

}