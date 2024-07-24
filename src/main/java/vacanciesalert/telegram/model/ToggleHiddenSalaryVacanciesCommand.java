package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.telegram.TelegramService;

@RequiredArgsConstructor
@Component
@Slf4j
public class ToggleHiddenSalaryVacanciesCommand implements UserCommand {
    private final TelegramService telegramService;

    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.TOGGLE_HIDDEN_SALARY.getCommand());
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        telegramService.sendYesNoButtonsMessage(
                chatId,
                "Вы хотите, чтобы отображались вакансии, где заработная плата НЕ указана?"
        );
    }
}
