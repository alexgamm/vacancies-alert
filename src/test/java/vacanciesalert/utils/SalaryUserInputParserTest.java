package vacanciesalert.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vacanciesalert.exception.InvalidSalaryRangeException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SalaryUserInputParserTest {
    @ParameterizedTest
    @MethodSource("positiveTestData")
    void exampleTest(String input, SalaryUserInputParser.Salary expected) throws Exception {
        assertEquals(expected, SalaryUserInputParser.parse(input));
    }

    @ParameterizedTest
    @MethodSource("negativeTestData")
    void exampleTest(String input, Class<Exception> expectedExceptionClass) {
        assertThrows(expectedExceptionClass, () -> SalaryUserInputParser.parse(input));
    }

    static Stream<Arguments> positiveTestData() {
        return Stream.of(
                Arguments.of("от 10 до 11222", new SalaryUserInputParser.Salary(10, 11222)),
                Arguments.of("от 10", new SalaryUserInputParser.Salary(10, null)),
                Arguments.of("от 1000000 до 1000000", new SalaryUserInputParser.Salary(1000000, null)),
                Arguments.of("от 0 до 100", new SalaryUserInputParser.Salary(0, 100)),
                Arguments.of("от10 до11222", new SalaryUserInputParser.Salary(10, 11222)),
                Arguments.of("от 10 до11222", new SalaryUserInputParser.Salary(10, 11222)),
                Arguments.of("от10до 11222", new SalaryUserInputParser.Salary(10, 11222))
        );
    }

    static Stream<Arguments> negativeTestData() {
        return Stream.of(
                Arguments.of("до 11222", InvalidSalaryRangeException.class),
                Arguments.of("от 1000000 до 100", InvalidSalaryRangeException.class),
                Arguments.of("от 1000001 до 1000000", InvalidSalaryRangeException.FromExceeded.class),
                Arguments.of("от 0 до 1000000000000", InvalidSalaryRangeException.class),
                Arguments.of("от 0 до 10000000", InvalidSalaryRangeException.ToExceeded.class),
                Arguments.of("от -4 до 100", InvalidSalaryRangeException.class),
                Arguments.of("от 3.4 до 100", InvalidSalaryRangeException.class),
                Arguments.of("3.4 до 100", InvalidSalaryRangeException.class),
                Arguments.of("3.4", InvalidSalaryRangeException.class),
                Arguments.of("от", InvalidSalaryRangeException.class)
        );
    }
}
