package umc.devapp.crud_produtos.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordServiceTest {

    @Test
    void shouldHashAndValidatePasswordWithBcrypt() {
        PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder(12));

        String rawPassword = "SenhaForte@123";
        String hash = passwordService.hashPassword(rawPassword);

        assertTrue(passwordService.matches(rawPassword, hash));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePasswordBecauseOfSalt() {
        PasswordService passwordService = new PasswordService(new BCryptPasswordEncoder(12));

        String rawPassword = "SenhaForte@123";
        String hash1 = passwordService.hashPassword(rawPassword);
        String hash2 = passwordService.hashPassword(rawPassword);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordService.matches(rawPassword, hash1));
        assertTrue(passwordService.matches(rawPassword, hash2));
    }
}
