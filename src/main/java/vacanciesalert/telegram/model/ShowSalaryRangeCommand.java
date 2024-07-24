package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.text.DecimalFormat;

@RequiredArgsConstructor
@Component
@Slf4j
public class ShowSalaryRangeCommand implements UserCommand {

    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    private final DecimalFormat decimalFormat;

    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.SHOW_SALARY_RANGE.getCommand());
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        UserInfo userInfo = userInfoRepository.findById(chatId).orElse(null);
        if (userInfo == null) {
            // TODO add exception
            return;
        }
        if (userInfo.getSalary().getFrom() == null) {
            telegramService.sendTextMessage(
                    chatId,
                    """
                            Вы еще не указали желаемый диапазон зарплаты.
                            Чтобы установить диапазон введите /setsalary
                            """
            );
        } else if (userInfo.getSalary().getTo() == null) {
            telegramService.sendTextMessage(
                    chatId,
                    String.format(
                            "На данный момент вам видны вакансии с заработной платой начиная от %s рублей",
                            decimalFormat.format(userInfo.getSalary().getFrom())
                    )
            );
        } else {
            telegramService.sendTextMessage(
                    chatId,
                    String.format(
                            "На данный момент вам видны вакансии с заработной платой от %s до %s рублей",
                            decimalFormat.format(userInfo.getSalary().getFrom()),
                            decimalFormat.format(userInfo.getSalary().getTo())
                    )
            );
        }
    }
}
