package gym.crm.service;

import gym.crm.exception.AuthenticationFailedException;
import gym.crm.exception.UserBlockedException;
import gym.crm.exception.ValidationException;
import gym.crm.metrics.GymMetricsRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final GymMetricsRecorder metricsRecorder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;

    public AuthenticationServiceImpl(GymMetricsRecorder metricsRecorder, AuthenticationManager authenticationManager,
                                     TokenService tokenService, TokenBlacklistService tokenBlacklistService,
                                     LoginAttemptService loginAttemptService) {
        this.metricsRecorder = metricsRecorder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public String authenticate(String username, String password) {
        if (username == null || password == null) {
            throw new ValidationException("Username or password missing");
        }
        if (loginAttemptService.isBlocked(username)) {
            log.warn("Blocked login attempt for locked user '{}'", username);
            throw new UserBlockedException(
                    "User is temporarily blocked due to too many failed login attempts. Please try again later.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password));
        } catch (AuthenticationException ex) {
            metricsRecorder.recordLoginAttempt(false);
            loginAttemptService.loginFailed(username);
            throw new AuthenticationFailedException("Credentials do not match");
        }

        if (!authentication.isAuthenticated()) {
            metricsRecorder.recordLoginAttempt(false);
            loginAttemptService.loginFailed(username);
            throw new AuthenticationFailedException("Credentials do not match");
        }

        metricsRecorder.recordLoginAttempt(true);
        loginAttemptService.loginSucceeded(username);
        return tokenService.generateToken(authentication);
    }

    @Override
    public void logout(String token) {
        log.info("Logging out user, blacklisting token");
        tokenBlacklistService.blacklist(token);
    }
}
