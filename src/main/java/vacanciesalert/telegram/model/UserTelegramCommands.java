package vacanciesalert.telegram.model;

import lombok.Getter;

@Getter
public enum UserTelegramCommands {

    START("/start"),
    SET_TAGS("/settags"),
    SHOW_TAGS("/showtags"),
    REMOVE_TAGS("/removetags"),
    STOP_BOT("/stop");

    private final String command;

    UserTelegramCommands(String command) {
        this.command = command;
    }
}
