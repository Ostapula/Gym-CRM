package gym.crm.controller;

import gym.crm.dto.ErrorMessage;
import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.EntityNotFoundException;
import gym.crm.exception.ProfileStatusException;
import gym.crm.exception.ValidationException;
import gym.crm.logging.TransactionContext;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentBecomesBadRequestErrorMessage() {
        MDC.put(TransactionContext.MDC_KEY, "tx-999");

        ResponseEntity<ErrorMessage> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("firstName is required"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Bad Request", response.getBody().getMessage());
        assertEquals("firstName is required", response.getBody().getDescription());
    }

    @Test
    void entityNotFoundBecomesNotFound() {
        ResponseEntity<ErrorMessage> response = handler.handleEntityNotFound(
                new EntityNotFoundException("Trainee username=ghost not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("Not Found", response.getBody().getMessage());
        assertEquals("Trainee username=ghost not found", response.getBody().getDescription());
    }

    @Test
    void authenticationFailedBecomesUnauthorized() {
        ResponseEntity<ErrorMessage> response = handler.handleAuthenticationFailed(
                new AuthenticationFailedException("Authentication failed for trainee username=john"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getCode());
    }

    @Test
    void validationBecomesBadRequest() {
        ResponseEntity<ErrorMessage> response = handler.handleValidation(
                new ValidationException("firstName is required"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getCode());
        assertEquals("firstName is required", response.getBody().getDescription());
    }

    @Test
    void profileStatusBecomesConflict() {
        ResponseEntity<ErrorMessage> response = handler.handleProfileStatus(
                new ProfileStatusException("Trainee profile is already active username=john"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getCode());
    }

    @Test
    void unexpectedExceptionBecomesInternalServerError() {
        ResponseEntity<ErrorMessage> response = handler.handleUnexpectedException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("Internal Server Error", response.getBody().getMessage());
        assertEquals("boom", response.getBody().getDescription());
    }
}

