package vacanciesalert.telegram;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.model.UserCommand;
import vacanciesalert.telegram.model.UserTelegramCommands;

import java.util.List;

@RestController
@RequestMapping("/telegram")
@Slf4j
@RequiredArgsConstructor
public class TelegramController {
    private final UserInfoRepository userInfoRepository;
    private final List<UserCommand> userCommands;

    @PostMapping("/webhook")
    @Transactional
    public void handleUpdate(@RequestBody Update update) {
        String userMessageText = update.getMessage().getText();
        log.info("update:{}", update);
        String chatId = String.valueOf(update.getMessage().getChatId());
        log.info("chatId:{}", chatId);
        for (UserCommand userCommand : userCommands) {
            if (userCommand.isRelevant(update.getMessage().getChatId(), update.getMessage().getText())) {
                userCommand.execute(update);
            }
            if (update.getMessage().getText().equals(UserTelegramCommands.SET_TAGS.getCommand())) {

            }

        }
        if (update.getChatMember() != null && update.getChatMember().getNewChatMember() != null &&
                "kicked".equals(update.getChatMember().getNewChatMember().getStatus())
        ) {
            userInfoRepository.deleteUserByChatId(Long.parseLong(chatId));
        } else {
            throw new IllegalStateException("Invalid command");
        }
    }
}


