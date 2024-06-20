package vacanciesalert.telegram.update;

import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;
import vacanciesalert.telegram.update.model.MultiselectTagsCursor;

import java.util.Map;

public interface CallbackHandler {

    boolean isRelevant(ButtonActionType action);

    default void handleCallback(Update update, MultiselectCallbackDataModel multiselectCallbackDataModel) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        handleSpecificCallback(chatId, multiselectCallbackDataModel);
    }

    void handleSpecificCallback(Long chatId, MultiselectCallbackDataModel multiselectCallbackDataModel);

    Map<Integer, MultiselectTagsCursor> getTagSelectionState();
}
