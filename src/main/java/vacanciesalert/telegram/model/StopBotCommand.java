package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
@RequiredArgsConstructor
@Component
public class StopBotCommand implements UserCommand{
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.STOP_BOT.getCommand());
    }

    @Override
    public void execute(Update update) {
        userInfoRepository.deleteUserById(update.getMessage().getChatId());
        telegramService.sendTextMessage(update.getMessage().getChatId().toString(),
                "Рассылка и поиск новых вакансий приостановлены. " +
                "Чтобы вновь начать пользоваться ботом напишите /start");
    }
}
