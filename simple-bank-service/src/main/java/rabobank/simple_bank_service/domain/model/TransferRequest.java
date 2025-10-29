package rabobank.simple_bank_service.domain.model;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
}
