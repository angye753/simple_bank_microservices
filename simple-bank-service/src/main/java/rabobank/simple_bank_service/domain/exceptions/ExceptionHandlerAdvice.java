package rabobank.simple_bank_service.domain.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(value = UserNotFound.class)
    public ResponseEntity<Map<String, String>> serviceErrorHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, String>> defaultErrorHandler(Exception exception) {
        return new ResponseEntity<>(createBody(exception, HttpStatus.INTERNAL_SERVER_ERROR.value()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, String> createBody(Exception exception, int status) {
        Map<String, String> body = new HashMap<>();
        body.put("status", String.valueOf(status));
        body.put("message", exception.getMessage());
        body.put("cause", getRootCause(exception));
        return body;
    }

    private String getRootCause(Exception exception) {
        return exception.getCause() != null ? sanitizeMessage(exception.getCause().getMessage()) :
                exception.getClass().getSimpleName();
    }

    private String sanitizeMessage(String causeMessage) {
        if (causeMessage.indexOf("trace") > 1) {
            causeMessage = causeMessage.substring(0, causeMessage.indexOf("trace"));
        }
        return causeMessage;
    }
}

