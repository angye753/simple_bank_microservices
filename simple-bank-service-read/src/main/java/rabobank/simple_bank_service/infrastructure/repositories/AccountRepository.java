package rabobank.simple_bank_service.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_service.infrastructure.entities.Account;
import rabobank.simple_bank_service.infrastructure.entities.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findById(String id);
    Optional<Account> findByAppUser(AppUser appUser);
    List<Account> findAll();

}

