package vacanciesalert.telegram;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import vacanciesalert.hh.oauth.AuthorizationService;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/telegram")
@Slf4j
public class TelegramController {
    private final UserInfoRepository userInfoRepository;
    private final TelegramService telegramService;
    private final List<UserCommand> userCommands;

    public TelegramController(
            UserInfoRepository userInfoRepository,
            TelegramService telegramService,
            AuthorizationService authorizationService
    ) {
        this.userInfoRepository = userInfoRepository;
        this.telegramService = telegramService;
        this.userCommands = List.of(
                new StartBotCommand(telegramService, userInfoRepository, authorizationService),
                new SetTagsCommand(telegramService, userInfoRepository),
                new StopBotCommand(telegramService, userInfoRepository),
                new ShowTagsCommand(telegramService, userInfoRepository, authorizationService),
                new RemoveTagsCommand(telegramService, userInfoRepository)
        );
    }

    @PostMapping("/webhook")
    @Transactional
    public void handleUpdate(@RequestBody Update update) {
        log.info("update:{}", update);
        String chatId = Optional.ofNullable(update.getMessage())
                .map(Message::getChatId)
                .map(String::valueOf)
                .orElse(null);
        log.info("chatId:{}", chatId);
        if (chatId != null) {
            for (UserCommand userCommand : userCommands) {
                if (userCommand.isRelevant(update.getMessage().getChatId(), update.getMessage().getText())) {
                    userCommand.execute(update);
                    return;
                }
            }
        }
        if (update.hasCallbackQuery()) {
            List<String> queryData = Arrays.stream(update.getCallbackQuery().getData().split(";")).map(String::trim).toList();
            if (ButtonActionTypes.REMOVE_TAG.toString().equals(queryData.get(0))) {
                String tag = queryData.get(1);
                log.info("Chosen tag: {}", tag);
                telegramService.editMessage(
                        update.getCallbackQuery().getMessage().getChatId(),
                        update.getCallbackQuery().getMessage().getMessageId(),
                        tag,
                        ButtonActionTypes.REMOVE_TAG
                );
                return;
            }
        }
        String newChatMemberStatus = Optional.ofNullable(update.getMyChatMember())
                .map(ChatMemberUpdated::getNewChatMember)
                .map(ChatMember::getStatus)
                .orElse(null);
        if ("kicked".equals(newChatMemberStatus)) {
            Long id = Optional.ofNullable(update.getMyChatMember().getFrom()).map(User::getId).orElse(0L);
            if (id == 0L) {
                log.error("Could not delete user info: chatId in telegram incoming update not found");
                return;
            }
            userInfoRepository.deleteUserById(id);
            return;
        }
        if (chatId != null) {
            telegramService.sendTextMessage(update.getMessage().getChatId().toString(),
                    "Я не понимаю о чем вы говорите. Попробуйте одну из поддерживаемых команд:\n" +
                            "/start - начать использование бота, заново авторизоваться на hh;\n" +
                            "/stop - прекратить происк свежих вакансий и остановить рассылку;\n" +
                            "/settags - удалить старые и задать новые теги;\n" +
                            "/mytags - посмотреть заданные теги, по которым осуществляется поиск вакансий;\n" +
                            "/removetags - удалить теги");

        }
    }
}


