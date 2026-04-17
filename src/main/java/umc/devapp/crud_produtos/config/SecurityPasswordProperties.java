package umc.devapp.crud_produtos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.password.bcrypt")
public record SecurityPasswordProperties(int strength) {
}
