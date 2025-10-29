package rabobank.simple_bank_service.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_service.infrastructure.entities.Ledger;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

}
