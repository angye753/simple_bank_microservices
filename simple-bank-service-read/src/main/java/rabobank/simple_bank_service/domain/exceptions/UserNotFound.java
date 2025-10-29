package rabobank.simple_bank_service.domain.exceptions;

public class UserNotFound  extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
