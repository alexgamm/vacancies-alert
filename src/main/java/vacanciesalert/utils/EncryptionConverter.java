package vacanciesalert.utils;

import jakarta.persistence.AttributeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionConverter implements AttributeConverter<String, String> {

    @Value("${aes.secret.key}")
    private String SECRET_KEY;

    @Override
    public String convertToDatabaseColumn(String toEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(toEncrypt.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with access token encrypting", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String toDecrypt) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(toDecrypt);
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new IllegalStateException("Something went wrong with access token decrypting", e);
        }
    }
}
