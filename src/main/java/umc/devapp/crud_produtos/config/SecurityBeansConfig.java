package umc.devapp.crud_produtos.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties({SecurityAuthProperties.class, SecurityPasswordProperties.class})
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder(SecurityPasswordProperties securityPasswordProperties) {
        return new BCryptPasswordEncoder(securityPasswordProperties.strength());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Autenticacao via /auth nao aceita UserDetailsService padrao");
        };
    }
}
