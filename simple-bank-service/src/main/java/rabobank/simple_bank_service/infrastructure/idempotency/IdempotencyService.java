package rabobank.simple_bank_service.infrastructure.idempotency;

public interface IdempotencyService {
    boolean checkAndMark(String idempotencyKey);
}