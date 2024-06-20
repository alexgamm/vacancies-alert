package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.telegram.tags.ButtonActionType;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ShowTagsCommand implements UserCommand {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;


    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.SHOW_TAGS.getCommand());
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        UserInfo userInfo = userInfoRepository.findById(chatId).orElse(null);
        if (userInfo == null) {
            //TODO add exception
        } else if (userInfo.getAccessToken() == null && (userInfo.getTags() == null || userInfo.getTags().isEmpty())) {
            telegramService.sendTextMessage(
                    chatId,
                    "На данный момент у вас нет установленных тегов.\n" +
                            "Также для поиска вакансий на hh вам необходимо пройти авторизацию"
            );
            Map<String, String> buttons = new HashMap<>(); // VacancyTagText key and uri value
            String buttonText = "Авторизация на hh";
            buttons.put(buttonText, authorizationService.createAuthUri(chatId.toString()));
            telegramService.sendAuthButtonMessage(
                    chatId,
                    "Чтобы искать вакансии для вас, необходимо перейти по кнопке ниже",
                    buttons
            );
        } else if (userInfo.getTags() == null || userInfo.getTags().isEmpty()) {
            telegramService.sendTextMessage(
                    chatId,
                    "На данный момент у вас нет установленных тегов."
            );
        } else {
            telegramService.sendTextMessage(
                    chatId,
                    "Ваши теги для поиска вакансий на hh:\n" +
                            String.join(", ", userInfo.getTags())
            );
        }
    }
}
