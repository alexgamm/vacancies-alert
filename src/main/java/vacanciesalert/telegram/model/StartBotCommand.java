package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.telegram.TelegramService;

@RequiredArgsConstructor
@Component
public class StartBotCommand implements UserCommand {
    private final TelegramService telegramService;

    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.START.getCommand());
    }

    @Override
    public void execute(Update update) {
        telegramService.sendButtonMessage(update.getMessage().getChatId().toString());
    }
}
