package rabobank.simple_bank_service.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class TransactionDTO {
    private Long id;
    private String senderAccountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String type;
    private String state;
}
