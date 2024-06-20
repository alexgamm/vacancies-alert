package vacanciesalert.utils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class SalaryUserInputParser {

    public static Salary parse(String input) throws Exception {
        Salary.SalaryBuilder builder = Salary.builder();
        if (input.contains("от")) {
            String[] parts = input.split(" ");
            int salaryFrom;
            if (parts[1].length() > 10) {
                salaryFrom = Integer.MAX_VALUE;
            } else {
                try {
                    salaryFrom = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    throw new Exception(e.getMessage());
                }
            }
            builder.from(salaryFrom);
            if (input.contains("до") && !(parts[1].length() > 10)) {
                int salaryTo;
                if (parts[3].length() > 10) {
                    salaryTo = Integer.MAX_VALUE;
                } else {
                    try {
                        salaryTo = Integer.parseInt(parts[3]);
                    } catch (NumberFormatException e) {
                        throw new Exception(e.getMessage());
                    }
                }
                return builder.to(salaryTo).build();
            }
        } else {
            throw new Exception("invalid user input");
        }
        return builder.build();
    }

    @RequiredArgsConstructor
    @Builder
    @Getter
    public static class Salary {
        private final Integer from;
        private final Integer to;
    }
}
