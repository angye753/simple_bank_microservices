package rabobank.simple_bank_service.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceDTO {
    private String accountNumber;
    private String ownerAccount;
    private BigDecimal balance;
}
