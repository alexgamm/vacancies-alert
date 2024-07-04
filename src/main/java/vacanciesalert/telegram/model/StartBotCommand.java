package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class StartBotCommand implements UserCommand {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;

    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.START.getCommand());
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        UserInfo userInfo = UserInfo.builder().chatId(chatId).build();
        userInfoRepository.save(userInfo);
        Map<String, String> buttons = new HashMap<>();
        String buttonText = "Авторизация на hh";
        buttons.put(buttonText, authorizationService.createAuthUri(chatId.toString()));
        telegramService.sendAuthButtonMessage(
                chatId,
                "Чтобы искать вакансии для вас, необходимо перейти по кнопке ниже",
                buttons
        );
    }
}
