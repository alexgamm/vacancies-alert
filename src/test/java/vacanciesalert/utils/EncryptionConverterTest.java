package vacanciesalert.utils;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncryptionConverterTest {
    @Test
    public void encodeAndDecodeToken() {
        //arrange
        EncryptionConverter encryptionConverter = new EncryptionConverter();
        String token = "Wk71AFBZeIuU8PlKe8552wIiKLliekmdtGOLKIGsd";
        ReflectionTestUtils.setField(encryptionConverter, "SECRET_KEY", "ohfztsN0SnDF2OdUtLCzjA==");
        //act
        String encodedToken = encryptionConverter.convertToDatabaseColumn(token);
        String decodedToken = encryptionConverter.convertToEntityAttribute(encodedToken);
        //assert
        assertEquals(token, decodedToken);
    }
}
