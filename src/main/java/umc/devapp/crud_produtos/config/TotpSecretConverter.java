package umc.devapp.crud_produtos.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Conversor JPA que criptografa/descriptografa o campo totp_secret
 * automaticamente ao persistir ou ler do banco de dados.
 *
 * Algoritmo: AES-256-GCM (Req. 3.5)
 * Chave: lida da variável de ambiente ENCRYPTION_KEY (Req. 3.6)
 * Dado em repouso: sempre cifrado (Req. 3.4)
 */
@Converter
public class TotpSecretConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private javax.crypto.SecretKey resolveKey() {
        String keyBase64 = System.getProperty("ENCRYPTION_KEY");
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException("ENCRYPTION_KEY não configurada. Configure no arquivo .env.");
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, resolveKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criptografar totp_secret antes de salvar no banco", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(dbData);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);

            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, resolveKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao descriptografar totp_secret lido do banco", e);
        }
    }
}
