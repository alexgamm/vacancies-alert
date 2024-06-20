package vacanciesalert.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;
import vacanciesalert.telegram.update.model.MultiselectTagsCursor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Component
public class NextTagsCallbackHandler implements CallbackHandler {
    private final TelegramService telegramService;
    private final UserInfoRepository userInfoRepository;

    @Override
    public boolean isRelevant(ButtonActionType action) {
        return action == ButtonActionType.NEXT_TAGS_PAGE;
    }

    @Override
    public void handleSpecificCallback(Long chatId, MultiselectCallbackDataModel multiselectCallbackDataModel) {
        Integer messageId = multiselectCallbackDataModel.getMessageId();
        MultiselectTagsCursor cursor = getTagSelectionState().get(messageId);
        Set<String> tags = userInfoRepository.findById(chatId)
                .orElseThrow()
                .getTags();
        if (tags == null || tags.isEmpty()) {
            // TODO delete message for tags removal
            return;
        }
        cursor.changeOffset(MultiselectTagsCursor.TAGS_PAGE_SIZE);
        telegramService.editMessage(chatId, messageId, cursor);
    }

    @Override
    public Map<Integer, MultiselectTagsCursor> getTagSelectionState() {
        return telegramService.getTagSelectionState();
    }
}
