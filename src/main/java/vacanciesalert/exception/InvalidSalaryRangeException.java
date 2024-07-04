package vacanciesalert.exception;

public class InvalidSalaryRangeException extends Exception {
    public static class FromExceeded extends InvalidSalaryRangeException {
    }

    public static class ToExceeded extends InvalidSalaryRangeException {
    }

}
