package vacanciesalert.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallbackDataParser {

    public static Map<String, Boolean> parseRemoveTagsCallbackData(String str) {
        // Разделить строку по ";"
        String[] parts = str.split(";");
        String action = parts[0].trim(); // REMOVE_TAG

        // Разделяем вторую часть по "."
        String[] subParts = parts[1].trim().split("\\.");

        String key = subParts[0].trim(); // Ключ для мапы с значением true
        List<String> falseValues = Arrays.asList(subParts[1].trim().split(",")); // split by ","

        // Создаем карту
        Map<String, Boolean> map = new HashMap<>();
        map.put(key, true);

        for (String item : falseValues) {
            if (item.trim().startsWith("✓")) {
                map.put(item.trim(), true);
            } else {
                map.put(item.trim(), false);
            }
        }

        return map;
    }
}
