package rabobank.simple_bank_service.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class LedgerDTO {
    private Long id;

    private AccountDTO owner;

    private BigDecimal amount;

    private String currency;

    private Long transactionId;

    private Instant createdAt;
}
