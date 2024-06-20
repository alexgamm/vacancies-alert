package vacanciesalert.telegram.model;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.utils.TagsParser;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class SetTagsCommand implements UserCommand {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;
    private final Set<Long> inputtingUsers = ConcurrentHashMap.newKeySet();


    @Override
    public boolean isRelevant(Long chatId, String command) {
        return command.equals(UserTelegramCommands.SET_TAGS.getCommand()) || inputtingUsers.contains(chatId);
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (inputtingUsers.contains(update.getMessage().getChatId())) {
            Set<String> tags = TagsParser.parse(update.getMessage().getText());
            userInfoRepository.updateTags(chatId, tags);
            userInfoRepository.updateSearchVacanciesFrom(chatId, Instant.now());
            String tagsText = String.join(", ", tags);
            String messageTemplate = """
                    Новые теги успешно сохранены: 
                    {0} 
                    Как только на hh появится новая, подходящая вам вакансия, она сразу же отобразится в этом чате
                    """;
            telegramService.sendTextMessage(chatId, MessageFormat.format(messageTemplate, tagsText));
            inputtingUsers.remove(chatId);
        } else {
            telegramService.sendTextMessage(chatId, """
                    Введите теги для поиска необходимых вам вакансий через запятую.
                    Например:
                    учитель русского языка, преподаватель физики и математики, заместитель директора""");
            inputtingUsers.add(chatId);
        }

    }
}
