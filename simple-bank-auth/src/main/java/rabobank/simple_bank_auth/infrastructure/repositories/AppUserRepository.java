package rabobank.simple_bank_auth.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_auth.infrastructure.entities.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, String> {
    Optional<AppUser> findById(String id);
    Optional<AppUser> findByUsername(String username);
}
