package rabobank.simple_bank_service.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_service.infrastructure.entities.Card;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findAll();
}

