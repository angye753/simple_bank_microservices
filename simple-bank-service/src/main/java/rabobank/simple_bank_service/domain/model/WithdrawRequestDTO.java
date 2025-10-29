package rabobank.simple_bank_service.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequestDTO {
    private String accountId;
    private BigDecimal amount;
}
