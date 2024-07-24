package vacanciesalert.telegram.model;

import lombok.Getter;

@Getter
public enum UserTelegramCommands {

    START("/start"),
    SET_TAGS("/settags"),
    SHOW_TAGS("/mytags"),
    REMOVE_TAGS("/removetags"),
    SET_SALARY_RANGE("/setsalary"),
    SHOW_SALARY_RANGE("/mysalaryrange"),
    TOGGLE_HIDDEN_SALARY("/hiddensalary"),
    STOP_BOT("/stop");

    private final String command;

    UserTelegramCommands(String command) {
        this.command = command;
    }
}
