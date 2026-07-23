package gym.crm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class LoginAttemptService {
    private final int maxAttempts;
    private final Duration blockDuration;
    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${security.brute-force.max-attempts}") int maxAttempts,
            @Value("${security.brute-force.block-duration-minutes}") long blockDurationMinutes) {
        this.maxAttempts = maxAttempts;
        this.blockDuration = Duration.ofMinutes(blockDurationMinutes);
    }

    public void loginFailed(String username) {
        if (username == null) {
            return;
        }
        String key = normalize(username);
        attempts.compute(key, (k, current) -> {
            int failures = (current == null ? 0 : current.failures()) + 1;
            Instant blockedUntil = failures >= maxAttempts ? Instant.now().plus(blockDuration) : null;
            if (blockedUntil != null) {
                log.warn("User '{}' blocked until {} after {} failed login attempts", k, blockedUntil, failures);
            }
            return new Attempt(failures, blockedUntil);
        });
    }

    public void loginSucceeded(String username) {
        if (username == null) {
            return;
        }
        attempts.remove(normalize(username));
    }

    public boolean isBlocked(String username) {
        if (username == null) {
            return false;
        }
        String key = normalize(username);
        Attempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil() == null) {
            return false;
        }
        if (Instant.now().isBefore(attempt.blockedUntil())) {
            return true;
        }

        attempts.remove(key);
        return false;
    }

    private String normalize(String username) {
        return username.toLowerCase();
    }

    private record Attempt(int failures, Instant blockedUntil) {
    }
}
