package vacanciesalert.telegram;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import vacanciesalert.telegram.tags.ButtonActionType;
import vacanciesalert.telegram.update.model.MultiselectTagsCursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class TelegramService {
    @Getter
    private final List<String> yesNoButtons = List.of("да", "нет");
    private final TelegramClient telegramClient;
    private final Map<Integer, MultiselectTagsCursor> tagSelectionState = new HashMap<>();

    @Value("${tg.bot.token}")
    private String token;

    private void execute(BotApiMethod<? extends Serializable> method) {
        try {
            telegramClient.execute(method);
        } catch (TelegramApiException e) {
            log.error("Telegram API error", e);
            throw new RuntimeException(e);
        }
    }

    public void deleteTgMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .messageId(messageId)
                .chatId(chatId)
                .build();
        execute(deleteMessage);
    }

    public void sendAuthButtonMessage(Long chatId, String messageText, Map<String, String> buttons) {
        List<InlineKeyboardButton> buttonsText = new ArrayList<>();
        for (String buttonText : buttons.keySet()) {
            buttonsText.add(
                    InlineKeyboardButton.builder()
                            .text(buttonText)
                            .url(buttons.get(buttonText)).build()
            );
        }
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(buttonsText.stream().map(InlineKeyboardRow::new).toList())
                .build();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .replyMarkup(markup)
                .build();
        execute(message);
    }

    public void sendYesNoButtonsMessage(Long chatId, String messageText) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        String approveButtonCallbackData = ButtonActionType.TOGGLE_HIDDEN_SALARY + " ; " + yesNoButtons.indexOf("да");
        String declineButtonCallbackData = ButtonActionType.TOGGLE_HIDDEN_SALARY + " ; " + yesNoButtons.indexOf("нет");
        for (String button : yesNoButtons) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text(button)
                            .callbackData(button.equals("да") ? approveButtonCallbackData : declineButtonCallbackData)
                            .build()
            );
        }
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(buttons.stream().map(InlineKeyboardRow::new).toList())
                .build();
        SendMessage message = SendMessage.builder().chatId(chatId).text(messageText).replyMarkup(markup).build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendRemoveTagsMessage(
            Long chatId,
            String messageText,
            Set<String> tags
    ) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        MultiselectTagsCursor multiselectTagsCursor = new MultiselectTagsCursor(tags.stream().sorted().toList());
        List<String> tagsOnTheCurrentPage = multiselectTagsCursor.getTagsOnTheCurrentPage();
        for (String tag : tagsOnTheCurrentPage) {
            String callbackData = ButtonActionType.TOGGLE_TAG + " ; " + tagsOnTheCurrentPage.indexOf(tag);
            log.info("Initial button callback: {}", callbackData);
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text(tag)
                            .callbackData(callbackData)
                            .build()
            );
        }
        if (tags.size() > MultiselectTagsCursor.TAGS_PAGE_SIZE) {
            buttons.add(createButton(ButtonActionType.NEXT_TAGS_PAGE, "->"));
        }

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(buttons.stream().map(InlineKeyboardRow::new).toList())
                .build();
        SendMessage message = SendMessage.builder().chatId(chatId).text(messageText).replyMarkup(markup).build();

        Message resultSendMessage;
        try {
            resultSendMessage = telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Telegram API error", e);
            throw new RuntimeException(e);
        }
        tagSelectionState.put(resultSendMessage.getMessageId(), multiselectTagsCursor);
    }

    public void sendTextMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .build();
        execute(message);
    }

    public void editTagsMessage(Long chatId, Integer messageId, MultiselectTagsCursor cursor) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        List<String> tags = cursor.getTagsOnTheCurrentPage();
        for (String tag : tags) {
            int tagIdx = tags.indexOf(tag);
            if (cursor.isSelected(tag)) {
                tag = "✓ " + tag;
            }
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(tag)
                    .callbackData(ButtonActionType.TOGGLE_TAG + " ; " + tagIdx)
                    .build();
            rows.add(new InlineKeyboardRow(button));
        }
        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();
        if (cursor.hasPrevTags()) {
            paginationButtons.add(createButton(ButtonActionType.PREV_TAGS_PAGE, "⬅"));
        }
        if (cursor.hasNextTags()) {
            paginationButtons.add(createButton(ButtonActionType.NEXT_TAGS_PAGE, "➡"));
        }
        if (!paginationButtons.isEmpty()) {
            rows.add(new InlineKeyboardRow(paginationButtons));
        }
        int selectedTagCount = cursor.selectedTagCount();
        if (selectedTagCount > 0) {
            rows.add(new InlineKeyboardRow(createButton(
                    ButtonActionType.REMOVE_SELECTED_TAGS,
                    String.format("Удалить выбранные теги %d", selectedTagCount)
            )));
        }
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(rows.stream().map(InlineKeyboardRow::new).toList())
                .build();
        execute(
                EditMessageReplyMarkup.builder()
                        .replyMarkup(markup)
                        .chatId(chatId)
                        .messageId(messageId)
                        .build()
        );
    }

    private InlineKeyboardButton createButton(ButtonActionType buttonActionType, String buttonText) {
        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(buttonActionType.toString())
                .build();
    }

}
