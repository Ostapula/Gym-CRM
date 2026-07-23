package gym.crm.service;

import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.UserBlockedException;
import gym.crm.exception.ValidationException;
import gym.crm.metrics.GymMetricsRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    private GymMetricsRecorder metricsRecorder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private TokenService tokenService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @InjectMocks
    private AuthenticationServiceImpl service;

    @Test
    void returnsTokenAndResetsAttemptsWhenCredentialsValid() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("john", "pass", java.util.List.of());
        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken(authentication)).thenReturn("jwt-token");

        String token = service.authenticate("john", "pass");

        assertEquals("jwt-token", token);
        verify(metricsRecorder).recordLoginAttempt(true);
        verify(loginAttemptService).loginSucceeded("john");
        verify(loginAttemptService, never()).loginFailed("john");
    }

    @Test
    void throwsAndRecordsFailureWhenCredentialsInvalid() {
        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("john", "wrong"));

        verify(metricsRecorder).recordLoginAttempt(false);
        verify(loginAttemptService).loginFailed("john");
        verifyNoInteractions(tokenService);
    }

    @Test
    void throwsWhenAuthenticationNotAuthenticated() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john", "pass");
        authentication.setAuthenticated(false);
        when(loginAttemptService.isBlocked("john")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThrows(AuthenticationFailedException.class, () -> service.authenticate("john", "pass"));

        verify(metricsRecorder).recordLoginAttempt(false);
        verify(loginAttemptService).loginFailed("john");
        verifyNoInteractions(tokenService);
    }

    @Test
    void throwsUserBlockedWhenUserIsBlockedAndSkipsAuthentication() {
        when(loginAttemptService.isBlocked("john")).thenReturn(true);

        assertThrows(UserBlockedException.class, () -> service.authenticate("john", "pass"));

        verifyNoInteractions(authenticationManager, tokenService, metricsRecorder);
        verify(loginAttemptService, never()).loginFailed("john");
    }

    @Test
    void throwsValidationWhenCredentialsMissingAndSkipsEverything() {
        assertThrows(ValidationException.class, () -> service.authenticate(null, "pass"));
        assertThrows(ValidationException.class, () -> service.authenticate("john", null));

        verifyNoInteractions(authenticationManager, tokenService, metricsRecorder, loginAttemptService);
    }

    @Test
    void logoutBlacklistsToken() {
        service.logout("some-token");

        verify(tokenBlacklistService).blacklist("some-token");
    }
}
