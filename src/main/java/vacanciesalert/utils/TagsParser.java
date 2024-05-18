package vacanciesalert.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TagsParser {
    public static Set<String> parse(String source) {
        String[] result = source.toLowerCase().split(",");
        return Arrays.stream(result).map(String::trim).collect(Collectors.toSet());
    }
}
