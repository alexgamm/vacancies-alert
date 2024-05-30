package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        } else if (userInfo.getAccessToken() == null) {
            telegramService.sendTextMessage(
                    chatId.toString(),
                    "На данный момент у вас нет установленных тегов.\n" +
                            "Также для поиска вакансий на hh вам необходимо пройти авторизацию"
            );
            Map<String, URI> buttons = new HashMap<>();
            String buttonText = "Авторизация на hh";
            buttons.put(buttonText, authorizationService.createAuthUri(chatId.toString()));
            telegramService.sendButtonMessage(
                    update.getMessage().getChatId().toString(),
                    "Чтобы искать вакансии для вас, необходимо перейти по кнопке ниже",
                    buttons,
                    ButtonActionTypes.AUTHORIZE_HH.toString()
            );
        } else if (userInfo.getTags() == null) {
            telegramService.sendTextMessage(
                    chatId.toString(),
                    "На данный момент у вас нет установленных тегов."
            );
        } else {
            telegramService.sendTextMessage(
                    chatId.toString(),
                    "Ваши теги для поиска вакансий на hh:\n" +
                            String.join(", ", userInfo.getTags())
            );
        }
    }
}
