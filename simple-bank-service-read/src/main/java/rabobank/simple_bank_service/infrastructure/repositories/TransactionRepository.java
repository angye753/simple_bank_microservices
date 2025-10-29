package rabobank.simple_bank_service.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_service.infrastructure.entities.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findById(Long id);
    List<Transaction> findAll();
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}

