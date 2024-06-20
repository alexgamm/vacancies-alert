package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.utils.SalaryUserInputParser;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class SetSalaryRangeCommand implements UserCommand {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    private final Set<Long> inputtingUsers = ConcurrentHashMap.newKeySet();


    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.SET_SALARY_RANGE.getCommand()) || inputtingUsers.contains(chatId);
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (inputtingUsers.contains(chatId)) {
            SalaryUserInputParser.Salary salaryRange;
            try {
                salaryRange = SalaryUserInputParser.parse(update.getMessage().getText());
            } catch (Exception e) {
                telegramService.sendTextMessage(
                        chatId,
                        """
                                Неправильный формат диапазона зарплаты.
                                Введите диапазон зарплат в рублях в формате:
                                от 100 до 20000
                                от 10000
                                """
                );
                log.warn("Invalid user input when setting salary range");
                return;
            }
            userInfoRepository.updateSalaryRange(chatId, salaryRange.getFrom(), salaryRange.getTo());
            telegramService.sendTextMessage(
                    chatId,
                    "Диапазон зарплаты успешно установлен."
            );
            inputtingUsers.remove(chatId);
        } else {
            telegramService.sendTextMessage(
                    chatId,
                    """
                            Введите диапазон желаемой заработной платы в рублях в формате:
                            от 100 до 20000
                            от 10000
                            """
            );
            inputtingUsers.add(chatId);
        }
    }
}
