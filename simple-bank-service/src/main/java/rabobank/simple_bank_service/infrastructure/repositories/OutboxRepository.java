package rabobank.simple_bank_service.infrastructure.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import rabobank.simple_bank_service.domain.model.OutboxEvent;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByPublishedFalse();
}

