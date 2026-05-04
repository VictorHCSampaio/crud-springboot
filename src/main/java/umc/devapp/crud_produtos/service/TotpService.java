package umc.devapp.crud_produtos.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;
import umc.devapp.crud_produtos.config.SecurityAuthProperties;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        String issuer = securityAuthProperties.totp().issuer();
        String label = issuer + ":" + username;
        String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String qrUri = "otpauth://totp/" + encodedLabel + "?secret=" + secret + "&issuer=" + encodedIssuer;
        System.out.println(qrUri);
        return qrUri;
    }

    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }
}
