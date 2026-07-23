package gym.crm.controller;

import gym.crm.dto.ErrorMessage;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.UserBlockedException;
import gym.crm.exception.ValidationException;
import gym.crm.logging.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleEntityNotFound(EntityNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorMessage> handleValidation(ValidationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorMessage> handleUserBlocked(UserBlockedException ex) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex);
    }

    @ExceptionHandler(ProfileStatusException.class)
    public ResponseEntity<ErrorMessage> handleProfileStatus(ProfileStatusException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorMessage> handleIllegalStateException(IllegalStateException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String description = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex, description);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String description = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex, description);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String description = String.format("Missing required parameter: %s", ex.getParameterName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex, description);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleUnexpectedException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(HttpStatus status, Exception ex) {
        return buildErrorResponse(status, ex, ex.getMessage());
    }

    private ResponseEntity<ErrorMessage> buildErrorResponse(HttpStatus status, Exception ex, String description) {
        String resolved = description == null || description.isBlank()
                ? status.getReasonPhrase()
                : description;
        log.error("REST error transactionId={} status={} description={}",
                TransactionContext.currentTransactionId().orElse("n/a"), status.value(), resolved, ex);
        return new ResponseEntity<>(new ErrorMessage(status.value(), status.getReasonPhrase(), resolved), status);
    }
}
