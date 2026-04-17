package umc.devapp.crud_produtos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.auth")
public record SecurityAuthProperties(
        int maxAttempts,
        int lockDurationMinutes,
        TotpProperties totp
) {

    public record TotpProperties(String issuer) {
    }
}
