package rabobank.simple_bank_service.application.in;

import java.math.BigDecimal;

public interface TransactionPort {


    Long transfer(String idempotencyKey,
                         String fromAccountId,
                         String toAccountId,
                         BigDecimal amount) throws Exception ;

    Long withdraw(String idempotencyKey,
                         String accountId,
                         BigDecimal amount) throws Exception;
}
