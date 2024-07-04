package vacanciesalert.utils;

import vacanciesalert.exception.InvalidSalaryRangeException;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalaryUserInputParser {

    public static final int MAX_BOUNDARY_LENGTH = 10;
    public static final int MAX_BOUNDARY_VALUE = 1_000_000;
    public static final Pattern PATTERN = Pattern.compile("(?:[\\sот]+)?([^-\\sдо]+)?(?:[-\\sдо]+)?(.+)?");

    public static Salary parse(String input) throws InvalidSalaryRangeException {
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.find()) {
            throw new InvalidSalaryRangeException();
        }
        Integer from = parseBoundary(matcher.group(1), InvalidSalaryRangeException.FromExceeded::new);
        if (from == null) {
            throw new InvalidSalaryRangeException();
        }
        Integer to = parseBoundary(matcher.group(2), InvalidSalaryRangeException.ToExceeded::new);
        if (to != null && to < from) {
            throw new InvalidSalaryRangeException();
        }
        return new Salary(from, from.equals(to) ? null : to);
    }

    private static Integer parseBoundary(
            String input,
            Supplier<InvalidSalaryRangeException> exceededBoundaryExceptionProvider
    ) throws InvalidSalaryRangeException {
        if (input == null) {
            return null;
        }
        if (input.length() > MAX_BOUNDARY_LENGTH) {
            throw exceededBoundaryExceptionProvider.get();
        }
        int parsed;
        try {
            parsed = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new InvalidSalaryRangeException();
        }
        if (parsed > MAX_BOUNDARY_VALUE) {
            throw exceededBoundaryExceptionProvider.get();
        }
        return parsed;
    }

    public record Salary(Integer from, Integer to) {
    }
}
