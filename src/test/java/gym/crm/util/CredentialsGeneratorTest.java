package gym.crm.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CredentialsGeneratorTest {

    private final CredentialsGenerator generator = new CredentialsGenerator();

    @Test
    void generatesPlainUsernameWhenNotTaken() {
        String username = generator.generateUsername("John", "Smith", taken -> false);
        assertEquals("John.Smith", username);
    }

    @Test
    void appendsSerialSuffixWhenUsernameTaken() {
        Set<String> existing = new HashSet<>();
        existing.add("John.Smith");
        existing.add("John.Smith1");

        String username = generator.generateUsername("John", "Smith", existing::contains);

        assertEquals("John.Smith2", username);
    }

    @Test
    void passwordHasTenCharacters() {
        String password = generator.generatePassword();
        assertEquals(10, password.length());
        assertTrue(password.chars().allMatch(Character::isLetterOrDigit));
    }

    @Test
    void passwordsAreRandom() {
        assertNotEquals(generator.generatePassword(), generator.generatePassword());
    }
}
