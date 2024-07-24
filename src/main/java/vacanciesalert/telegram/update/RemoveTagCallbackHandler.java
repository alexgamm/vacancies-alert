package vacanciesalert.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;
import vacanciesalert.telegram.update.model.MultiselectTagsCursor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class RemoveTagCallbackHandler implements CallbackHandler {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;

    @Override
    public boolean isRelevant(ButtonActionType action) {
        return action == ButtonActionType.REMOVE_SELECTED_TAGS;
    }

    @Override
    public void handleSpecificCallback(Long chatId, MultiselectCallbackDataModel multiselectCallbackData) {
        Integer messageId = multiselectCallbackData.getMessageId();
        Set<String> selectedTags = getTagSelectionState().get(messageId).getSelectedTags();
        log.info("Tags to delete: {}", String.join(", ", selectedTags));
        Set<String> tags = Optional.of(userInfoRepository.findById(chatId)).get().map(UserInfo::getTags).orElseThrow();
        Set<String> tagsToPreserve = tags.stream()
                .filter(tag -> !selectedTags.contains(tag))
                .collect(Collectors.toSet());
        userInfoRepository.updateTags(chatId, tagsToPreserve.toArray(String[]::new));

        String message = tagsToPreserve.isEmpty() ?
                "/settags - установите теги, чтобы искать вакансии" :
                "Поиск будет осуществляться по следующим ключевым словам:\n" + String.join(", ", tagsToPreserve);
        telegramService.sendTextMessage(
                chatId,
                "Теги успешно удалены.\n" + message
        );
        getTagSelectionState().remove(messageId);
        telegramService.deleteTgMessage(chatId, messageId);
    }

    public Map<Integer, MultiselectTagsCursor> getTagSelectionState() {
        return telegramService.getTagSelectionState();
    }
}
