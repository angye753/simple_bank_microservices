package rabobank.simple_bank_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;
    private String aggregateId;
    private String type; // e.g. TransactionPosted
    @Lob
    private String payload; // JSON
    private boolean published;
    private java.time.Instant createdAt;
}

