package vacanciesalert.telegram.model;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserCommand {
    boolean isRelevant(Long chatId, String command);
    void execute(Update update);
}
