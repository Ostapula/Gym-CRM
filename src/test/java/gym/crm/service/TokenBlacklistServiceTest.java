package gym.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBlacklistServiceTest {

    private TokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new TokenBlacklistService();
    }

    @Test
    void tokenIsNotBlacklistedByDefault() {
        assertFalse(service.isBlacklisted("token-1"));
    }

    @Test
    void blacklistedTokenIsReportedAsBlacklisted() {
        service.blacklist("token-1");

        assertTrue(service.isBlacklisted("token-1"));
    }

    @Test
    void onlyTheBlacklistedTokenIsAffected() {
        service.blacklist("token-1");

        assertTrue(service.isBlacklisted("token-1"));
        assertFalse(service.isBlacklisted("token-2"));
    }

    @Test
    void blacklistingSameTokenTwiceKeepsItBlacklisted() {
        service.blacklist("token-1");
        service.blacklist("token-1");

        assertTrue(service.isBlacklisted("token-1"));
    }
}
