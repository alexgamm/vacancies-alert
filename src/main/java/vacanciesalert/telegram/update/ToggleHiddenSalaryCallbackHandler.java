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

@RequiredArgsConstructor
@Slf4j
@Component
public class ToggleHiddenSalaryCallbackHandler implements CallbackHandler {
    private final UserInfoRepository userInfoRepository;
    private final TelegramService telegramService;

    @Override
    public boolean isRelevant(ButtonActionType action) {
        return action == ButtonActionType.TOGGLE_HIDDEN_SALARY;
    }

    @Override
    public void handleSpecificCallback(Long chatId, MultiselectCallbackDataModel multiselectCallbackDataModel) {
        String chosenButton = multiselectCallbackDataModel.getChosenButton();
        Integer messageId = multiselectCallbackDataModel.getMessageId();
        boolean showHiddenSalary = multiselectCallbackDataModel.getChosenButton().equals("да");
        userInfoRepository.toggleHiddenSalaryVacancies(
                chatId,
                showHiddenSalary
        );
        telegramService.deleteTgMessage(chatId, messageId);
        telegramService.sendTextMessage(
                chatId,
                String.format("Поздравляю! Теперь вам %s будут видны вакансии без зарпаты", showHiddenSalary ? "" : "не")
        );
    }

    @Override
    public Map<Integer, MultiselectTagsCursor> getTagSelectionState() {
        return telegramService.getTagSelectionState();
    }
}
