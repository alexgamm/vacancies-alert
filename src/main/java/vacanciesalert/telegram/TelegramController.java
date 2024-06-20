package vacanciesalert.telegram;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.model.UserCommand;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.CallbackHandler;
import vacanciesalert.telegram.update.CallbackParser;
import vacanciesalert.telegram.update.NextTagsCallbackHandler;
import vacanciesalert.telegram.update.PrevTagsCallbackHandler;
import vacanciesalert.telegram.update.RemoveTagCallbackHandler;
import vacanciesalert.telegram.update.SelectTagCallbackHandler;
import vacanciesalert.telegram.update.UpdateHandler;
import vacanciesalert.telegram.update.UpdateType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/telegram")
@Slf4j
public class TelegramController {
    private final UserInfoRepository userInfoRepository;
    private final TelegramService telegramService;
    private final List<UserCommand> userCommands;
    private final Map<ButtonActionType, CallbackHandler> callbackHandlers;

    public TelegramController(
            UserInfoRepository userInfoRepository,
            TelegramService telegramService,
            List<UserCommand> userCommands,
            NextTagsCallbackHandler nextTagsCallbackHandler,
            PrevTagsCallbackHandler prevTagsCallbackHandler,
            RemoveTagCallbackHandler removeCallbackHandler,
            SelectTagCallbackHandler selectTagCallbackHandler
    ) {
        this.userInfoRepository = userInfoRepository;
        this.telegramService = telegramService;
        this.userCommands = userCommands;
        this.callbackHandlers = Map.of(
                ButtonActionType.NEXT_TAGS_PAGE, nextTagsCallbackHandler,
                ButtonActionType.PREV_TAGS_PAGE, prevTagsCallbackHandler,
                ButtonActionType.REMOVE_SELECTED_TAGS, removeCallbackHandler,
                ButtonActionType.TOGGLE_TAG, selectTagCallbackHandler
        );
    }

    @PostMapping("/webhook")
    @Transactional
    public void handleUpdate(@RequestBody Update update) {
        UpdateType updateType = UpdateHandler.handleUpdate(update);

        if (updateType == UpdateType.USER_COMMAND) {
            for (UserCommand userCommand : userCommands) {
                if (userCommand.isRelevant(update.getMessage().getChatId(), update.getMessage().getText())) {
                    userCommand.execute(update);
                    return;
                }
            }
        }
        if (updateType == UpdateType.CALLBACK) {
            MultiselectCallbackDataModel multiselectCallbackDataModel = CallbackParser.parseCallbackData(
                    update,
                    telegramService.getTagSelectionState()
                            .get(update.getCallbackQuery().getMessage().getMessageId())
                            .getTagsOnTheCurrentPage()
            );
            ButtonActionType buttonActionType = multiselectCallbackDataModel.getButtonActionType();
            CallbackHandler callbackHandler = callbackHandlers.get(buttonActionType);
            if (callbackHandler == null) {
                log.error("No relevant callback handler for button action type {}", buttonActionType);
                return;
            }
            callbackHandler.handleCallback(update, multiselectCallbackDataModel);
            return;
        }
        if (updateType == UpdateType.USER_BOT_BLOCK) {
            Long id = Optional.ofNullable(update.getMyChatMember().getFrom()).map(User::getId).orElse(0L);
            if (id == 0L) {
                log.error("Could not delete user info: chatId in telegram incoming update not found");
                return;
            }
            userInfoRepository.deleteUserById(id);
            return;
        }
        telegramService.sendTextMessage(
                update.getMessage().getChatId(),
                "Я не понимаю о чем вы говорите. Попробуйте одну из поддерживаемых команд:\n" +
                        "/start - начать использование бота, заново авторизоваться на hh;\n" +
                        "/stop - прекратить происк свежих вакансий и остановить рассылку;\n" +
                        "/settags - удалить старые и задать новые теги;\n" +
                        "/mytags - посмотреть заданные теги, по которым осуществляется поиск вакансий;\n" +
                        "/removetags - удалить теги"
        );
    }
}


