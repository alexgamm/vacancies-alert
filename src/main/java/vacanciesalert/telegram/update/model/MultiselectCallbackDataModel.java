package vacanciesalert.telegram.update.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vacanciesalert.telegram.tags.ButtonActionType;

@RequiredArgsConstructor
@Getter
@Builder
public class MultiselectCallbackDataModel {
    private final Integer messageId;
    private final ButtonActionType buttonActionType;
    private final String chosenButton;
}
