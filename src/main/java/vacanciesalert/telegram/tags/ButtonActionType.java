package vacanciesalert.telegram.tags;

import lombok.Getter;

@Getter
public enum ButtonActionType {
    TOGGLE_TAG,
    REMOVE_SELECTED_TAGS,
    NEXT_TAGS_PAGE,
    PREV_TAGS_PAGE,
    TOGGLE_HIDDEN_SALARY
}
