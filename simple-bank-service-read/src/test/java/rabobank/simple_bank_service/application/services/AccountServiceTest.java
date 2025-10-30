package rabobank.simple_bank_service.application.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import rabobank.simple_bank_service.application.auth.SecurityUtils;
import rabobank.simple_bank_service.application.mappers.AccountMapper;
import rabobank.simple_bank_service.domain.exceptions.UserNotFound;
import rabobank.simple_bank_service.domain.model.AccountDTO;
import rabobank.simple_bank_service.domain.model.BalanceDTO;
import rabobank.simple_bank_service.infrastructure.entities.Account;
import rabobank.simple_bank_service.infrastructure.entities.AppUser;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;
import rabobank.simple_bank_service.infrastructure.repositories.AppUserRepository;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private AppUser adminUser;
    private AppUser normalUser;
    private Account account1;
    private Account account2;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        adminUser = new AppUser();
        adminUser.setId("U1");
        adminUser.setUsername("admin");
        adminUser.setRole("admin");
        adminUser.setName("Admin User");

        normalUser = new AppUser();
        normalUser.setId("U2");
        normalUser.setUsername("john");
        normalUser.setRole("user");
        normalUser.setName("John Doe");

        account1 = new Account();
        account1.setId("A1");
        account1.setAppUser(adminUser);
        account1.setBalance(BigDecimal.valueOf(1000));

        account2 = new Account();
        account2.setId("A2");
        account2.setAppUser(normalUser);
        account2.setBalance(BigDecimal.valueOf(500));

        mockedSecurityUtils = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void getAllAccounts_ShouldReturnMappedAccounts() {
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(accountMapper.toDTO(account1)).thenReturn(AccountDTO.builder().id("A1").build());
        when(accountMapper.toDTO(account2)).thenReturn(AccountDTO.builder().id("A2").build());

        List<AccountDTO> result = accountService.getAllAccounts();

        assertEquals(2, result.size());
        verify(accountRepository).findAll();
        verify(accountMapper, times(2)).toDTO(any(Account.class));
    }

    @Test
    void getAccountBalance_AdminUser_ShouldReturnBalances() throws Exception {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("admin");
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        List<BalanceDTO> result = accountService.getAccountBalance();

        assertEquals(2, result.size());
        verify(accountRepository).findAll();
    }

    @Test
    void getAccountBalance_NonAdminUser_ShouldThrowAccessDenied() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("john");
        when(appUserRepository.findByUsername("john")).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () -> accountService.getAccountBalance());
    }

    @Test
    void getAccountBalance_UserNotFound_ShouldThrowUserNotFound() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("ghost");
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> accountService.getAccountBalance());
    }

    @Test
    void getAccountBalance_ByUserId_ShouldReturnBalanceWhenAuthorized() throws Exception {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("john");
        when(appUserRepository.findById("U2")).thenReturn(Optional.of(normalUser));
        when(accountRepository.findByAppUser(normalUser)).thenReturn(Optional.of(account2));

        List<BalanceDTO> result = accountService.getAccountBalance("U2");

        assertEquals(1, result.size());
        assertEquals("A2", result.get(0).getAccountNumber());
        verify(accountRepository).findByAppUser(normalUser);
    }

    @Test
    void getAccountBalance_ByUserId_ShouldThrowAccessDenied_WhenDifferentUser() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("jane");
        when(appUserRepository.findById("U2")).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () -> accountService.getAccountBalance("U2"));
    }

    @Test
    void getAccountBalance_ByUserId_ShouldThrowUserNotFound() {
        mockedSecurityUtils.when(SecurityUtils::getCurrentUsername).thenReturn("john");
        when(appUserRepository.findById("U3")).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> accountService.getAccountBalance("U3"));
    }
}
