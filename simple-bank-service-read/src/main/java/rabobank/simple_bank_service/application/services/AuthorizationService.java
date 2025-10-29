package rabobank.simple_bank_service.application.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import rabobank.simple_bank_service.infrastructure.repositories.AccountRepository;

@Component("authz")
public class AuthorizationService {

    private final AccountRepository accountRepo;

    public AuthorizationService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public boolean isOwner(String accountId, Authentication auth) {
        String userId;

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            userId = jwtAuth.getTokenAttributes().get("subject").toString();
        } else if (auth.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            userId = principal.getAttribute("sub");
        } else {
            userId = null;
        }

        return accountRepo.findById(accountId)
                .map(a -> a.getAppUser().getId().equals(userId))
                .orElse(false);
    }
}
