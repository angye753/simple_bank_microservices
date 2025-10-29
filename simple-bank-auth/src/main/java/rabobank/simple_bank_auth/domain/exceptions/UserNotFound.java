package rabobank.simple_bank_auth.domain.exceptions;

public class UserNotFound  extends RuntimeException {
    public UserNotFound(String message) {
        super(message);
    }
}
