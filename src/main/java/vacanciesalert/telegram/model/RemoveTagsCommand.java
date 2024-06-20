package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.telegram.tags.ButtonActionType;

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
        } else if (userInfo.getTags() == null || userInfo.getTags().isEmpty()) {
            telegramService.sendTextMessage(
                    update.getCallbackQuery().getMessage().getChatId(),
                    "На данный момент у вас нет установленных тегов."
            );
        } else {
            telegramService.sendRemoveTagsMessage(
                    chatId,
                    "Выберите теги, которые хотите удалить:",
                    userInfo.getTags()
            );
        }
    }
}
