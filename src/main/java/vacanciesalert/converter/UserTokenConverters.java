package vacanciesalert.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import vacanciesalert.hh.oauth.model.UserTokens;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class UserTokenConverters {
    @Component
    @ReadingConverter
    @RequiredArgsConstructor
    public static class Reading implements Converter<String, UserTokens> {
        private final ObjectMapper objectMapper;
        @Value("${aes.secret.key}")
        private String secretKey;

        @Override
        public UserTokens convert(@NotNull String toDecrypt) {
            try {
                byte[] encrypted = Base64.getDecoder().decode(toDecrypt);
                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                String decrypted = new String(cipher.doFinal(encrypted));
                return objectMapper.readValue(decrypted, UserTokens.class);
            } catch (Exception e) {
                throw new IllegalStateException("Something went wrong with access token decrypting", e);
            }
        }
    }

    @Component
    @WritingConverter
    @RequiredArgsConstructor
    public static class Writing implements Converter<UserTokens, String> {
        private final ObjectMapper objectMapper;
        @Value("${aes.secret.key}")
        private String secretKey;

        @Override
        public String convert(@NotNull UserTokens tokens) {
            try {
                Cipher cipher = Cipher.getInstance("AES");
                SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                byte[] encrypted = cipher.doFinal(objectMapper.writeValueAsString(tokens).getBytes());
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (Exception e) {
                throw new IllegalStateException("Something went wrong with access token encrypting", e);
            }
        }
    }
}
