package gym.crm.controller;

import gym.crm.dto.ErrorMessage;
import gym.crm.logging.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalStateException(IllegalStateException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleUnexpectedException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(HttpStatus status, Exception ex) {
        String description = ex.getMessage() == null || ex.getMessage().isBlank()
                ? status.getReasonPhrase()
                : ex.getMessage();
        log.error("REST error transactionId={} status={} description={}",
                TransactionContext.currentTransactionId().orElse("n/a"), status.value(), description, ex);
        return new ResponseEntity<>(new ErrorMessage(status.value(), status.getReasonPhrase(), description), status);
    }
}

