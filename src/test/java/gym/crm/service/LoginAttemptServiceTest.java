package gym.crm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private LoginAttemptService newService() {
        return new LoginAttemptService(3, 5);
    }

    @Test
    void notBlockedBeforeReachingMaxAttempts() {
        LoginAttemptService service = newService();
        service.loginFailed("john");
        service.loginFailed("john");
        assertFalse(service.isBlocked("john"));
    }

    @Test
    void blocksAfterThreeFailedAttempts() {
        LoginAttemptService service = newService();
        service.loginFailed("john");
        service.loginFailed("john");
        service.loginFailed("john");
        assertTrue(service.isBlocked("john"));
    }

    @Test
    void countsAreCaseInsensitivePerUsername() {
        LoginAttemptService service = newService();
        service.loginFailed("John");
        service.loginFailed("jOHN");
        service.loginFailed("JOHN");
        assertTrue(service.isBlocked("john"));
    }

    @Test
    void successfulLoginResetsFailureCount() {
        LoginAttemptService service = newService();
        service.loginFailed("john");
        service.loginFailed("john");
        service.loginSucceeded("john");
        service.loginFailed("john");
        service.loginFailed("john");
        assertFalse(service.isBlocked("john"));
    }

    @Test
    void differentUsersAreTrackedIndependently() {
        LoginAttemptService service = newService();
        service.loginFailed("john");
        service.loginFailed("john");
        service.loginFailed("john");
        assertTrue(service.isBlocked("john"));
        assertFalse(service.isBlocked("jane"));
    }

    @Test
    void immediatelyUnblockedWhenWindowIsZero() {
        LoginAttemptService service = new LoginAttemptService(3, 0);
        service.loginFailed("john");
        service.loginFailed("john");
        service.loginFailed("john");
        assertFalse(service.isBlocked("john"));
    }

    @Test
    void nullUsernameIsNeverBlocked() {
        LoginAttemptService service = newService();
        service.loginFailed(null);
        assertFalse(service.isBlocked(null));
    }
}
