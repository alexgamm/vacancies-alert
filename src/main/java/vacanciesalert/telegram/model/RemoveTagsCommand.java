package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class RemoveTagsCommand implements UserCommand {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;


    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.REMOVE_TAGS.getCommand());
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        UserInfo userInfo = userInfoRepository.findById(chatId).orElse(null);
        if (userInfo == null) {
            //TODO add exception
        } else if (userInfo.getTags() == null) {
            telegramService.sendTextMessage(
                    chatId.toString(),
                    "На данный момент у вас нет установленных тегов."
            );
        } else {
            Map<String, URI> buttons = new HashMap<>();
            for (String tag : userInfo.getTags()) {
                buttons.put(tag, null);
            }
            telegramService.sendButtonMessage(
                    chatId.toString(),
                    "Выберите теги, которые хотите удалить:",
                    buttons,
                    ButtonActionTypes.REMOVE_TAG.toString()
            );
        }


    }
}
