package umc.devapp.crud_produtos.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class AuthSecurityPropertiesTest {

    @Test
    void shouldKeepExpectedBcryptAndBruteForceDefaults() {
        SecurityPasswordProperties passwordProperties = new SecurityPasswordProperties(12);
        SecurityAuthProperties authProperties = new SecurityAuthProperties(
                5,
                15,
                new SecurityAuthProperties.TotpProperties("crud-produtos")
        );

        assertEquals(12, passwordProperties.strength());
        assertEquals(5, authProperties.maxAttempts());
        assertEquals(15, authProperties.lockDurationMinutes());
        assertEquals("crud-produtos", authProperties.totp().issuer());
    }
}
