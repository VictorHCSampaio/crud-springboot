package umc.devapp.crud_produtos.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;
import umc.devapp.crud_produtos.config.SecurityAuthProperties;

@Service
public class TotpService {

    private final GoogleAuthenticator googleAuthenticator;
    private final SecurityAuthProperties securityAuthProperties;

    public TotpService(SecurityAuthProperties securityAuthProperties) {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setWindowSize(1)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
        this.securityAuthProperties = securityAuthProperties;
    }

    public String generateSecret() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    public String buildQrUri(String username, String secret) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(
                securityAuthProperties.totp().issuer(),
                username,
                new GoogleAuthenticatorKey.Builder(secret).build()
        );
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }
}
