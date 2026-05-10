package umc.devapp.crud_produtos.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TotpSecretConverterTest {

    private static final TotpSecretConverter CONVERTER = new TotpSecretConverter();

    @BeforeAll
    static void setEncryptionKey() {
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0x41);
        System.setProperty("ENCRYPTION_KEY", Base64.getEncoder().encodeToString(key));
    }

    @Test
    void roundTripEncryptThenDecrypt() {
        String secret = "JBSWY3DPEHPK3PXP";
        String stored = CONVERTER.convertToDatabaseColumn(secret);
        assertEquals(secret, CONVERTER.convertToEntityAttribute(stored));
    }

    @Test
    void legacyPlainSecretNotValidBase64ReturnedAsIs() {
        String legacy = "PLAIN-TOTP-SECRET-NOT-BASE64!!!";
        assertEquals(legacy, CONVERTER.convertToEntityAttribute(legacy));
    }

    @Test
    void legacyBase32DecodedTooShortReturnedAsIs() {
        String legacyBase32 = "JBSWY3DPEHPK3PXP";
        assertEquals(legacyBase32, CONVERTER.convertToEntityAttribute(legacyBase32));
    }

    @Test
    void base64DecodedUnderMinLengthReturnedAsIs() {
        byte[] shortBlob = new byte[10];
        Arrays.fill(shortBlob, (byte) 9);
        String b64 = Base64.getEncoder().encodeToString(shortBlob);
        assertEquals(b64, CONVERTER.convertToEntityAttribute(b64));
    }
}
