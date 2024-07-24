package vacanciesalert.telegram.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectCallbackDataModel;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CallbackParser {

    public static ButtonActionType getButtonType(String callbackData) {
        List<String> queryData = Arrays.stream(callbackData.split(";")).map(String::trim).toList();
        log.info("Query data: {}", String.join(" ", queryData));
        return ButtonActionType.valueOf(queryData.get(0));
    }

    public static MultiselectCallbackDataModel parseCallbackData(
            Update update,
            List<String> buttonsOnTheCurrentPage
    ) {
        List<String> queryData = Arrays
                .stream(update.getCallbackQuery().getData().split(";"))
                .map(String::trim)
                .toList();
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        MultiselectCallbackDataModel.MultiselectCallbackDataModelBuilder builder = MultiselectCallbackDataModel.builder()
                .messageId(messageId)
                .buttonActionType(ButtonActionType.valueOf(queryData.get(0)));
        if (queryData.size() == 1) {
            return builder.build();
        } else {
            return builder.chosenButton(buttonsOnTheCurrentPage.get(Integer.parseInt(queryData.get(1).trim()))).build();
        }

    }
}
