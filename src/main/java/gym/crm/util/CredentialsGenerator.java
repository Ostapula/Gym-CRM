package gym.crm.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.function.Predicate;

@Slf4j
@Component
public class CredentialsGenerator {
    private static final String DATA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Predicate<String> usernameExists) {
        String base = firstName + "." + lastName;
        String candidate = base;
        int serial = 1;
        while (usernameExists.test(candidate)) {
            candidate = base + serial;
            serial++;
        }
        log.debug("Generated unique username candidate with duplicateCount={}", serial - 1);
        return candidate;
    }

    public String generatePassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int index = RANDOM.nextInt(DATA.length());
            sb.append(DATA.charAt(index));
        }
        String generatedPassword = sb.toString();
        log.debug("Generated password with policy: length=10, charset=alphanumeric");
        return generatedPassword;
    }
}
