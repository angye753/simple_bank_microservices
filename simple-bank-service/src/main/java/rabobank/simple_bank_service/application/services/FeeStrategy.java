package rabobank.simple_bank_service.application.services;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public interface FeeStrategy {
    BigDecimal fee(BigDecimal amount);
}

@Component("credit")
class CreditFeeStrategy implements FeeStrategy {
    private static final BigDecimal RATE = new BigDecimal("0.01");
    public BigDecimal fee(BigDecimal amount) {
        return amount.multiply(RATE).setScale(2, java.math.RoundingMode.HALF_EVEN);
    }
}
@Component("debit")
class DebitFeeStrategy implements FeeStrategy {
    public BigDecimal fee(BigDecimal amount) { return BigDecimal.ZERO; }
}