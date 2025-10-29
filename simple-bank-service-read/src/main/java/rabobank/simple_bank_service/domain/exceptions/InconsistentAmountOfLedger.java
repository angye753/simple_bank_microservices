package rabobank.simple_bank_service.domain.exceptions;

public class InconsistentAmountOfLedger extends RuntimeException {
    public InconsistentAmountOfLedger(String message) {
        super(message);
    }
}
