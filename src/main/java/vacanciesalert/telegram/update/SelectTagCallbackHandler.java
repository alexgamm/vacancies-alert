package vacanciesalert.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vacanciesalert.telegram.TelegramService;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;
import vacanciesalert.telegram.update.model.MultiselectTagsCursor;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class SelectTagCallbackHandler implements CallbackHandler {
    private final TelegramService telegramService;

    @Override
    public boolean isRelevant(ButtonActionType action) {
        return action == ButtonActionType.TOGGLE_TAG;
    }

    @Override
    public void handleSpecificCallback(Long chatId, MultiselectCallbackDataModel multiselectCallbackDataModel) {
        String chosenTag = multiselectCallbackDataModel.getChosenButton();
        Integer messageId = multiselectCallbackDataModel.getMessageId();
        MultiselectTagsCursor cursor = getTagSelectionState().get(messageId);
        if (cursor == null) {
            // TODO send RemoveTagsMessage
            log.error("Could not find tags for message id {}", messageId);
            return;
        }
        cursor.toggleTag(chosenTag);
        telegramService.editMessage(chatId, messageId, cursor);
    }

    @Override
    public Map<Integer, MultiselectTagsCursor> getTagSelectionState() {
        return telegramService.getTagSelectionState();
    }
}
