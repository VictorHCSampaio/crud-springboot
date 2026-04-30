package umc.devapp.crud_produtos.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Serviço de criptografia simétrica usando AES-256-GCM.
 *
 * Justificativa técnica (Req. 3.8):
 * - AES-256: padrão NIST, chave de 256 bits — resistente a ataques de força bruta
 *   e aprovado para dados classificados até SECRET pelo CNSS/NSA.
 * - GCM (Galois/Counter Mode): modo autenticado (AEAD) que garante
 *   confidencialidade E integridade do dado em uma única operação,
 *   detectando adulteração silenciosa.
 * - IV aleatório de 12 bytes gerado por SecureRandom a cada cifração:
 *   garante que textos idênticos produzam cifras diferentes (IND-CPA).
 * - A chave é lida da variável de ambiente ENCRYPTION_KEY (Req. 3.6),
 *   nunca embutida no código-fonte.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;

    public EncryptionService() {
        String keyBase64 = System.getProperty("ENCRYPTION_KEY");
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException(
                    "Variável ENCRYPTION_KEY não configurada. " +
                    "Gere uma chave com: openssl rand -base64 32"
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY deve ter exatamente 32 bytes (AES-256). " +
                    "Certifique-se de gerar com: openssl rand -base64 32"
            );
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes());

            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao criptografar dado sensível", e);
        }
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null) return null;
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(ciphertext);

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);

            byte[] encrypted = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao descriptografar dado sensível", e);
        }
    }
}
