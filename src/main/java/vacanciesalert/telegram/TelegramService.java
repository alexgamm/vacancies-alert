package vacanciesalert.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.model.ButtonActionTypes;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {

    private final UserInfoRepository userInfoRepository;

    private final WebClient webClient;

    @Value("${tg.bot.token}")
    private String token;


    private void sendTgMessage(SendMessage message) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", token);
        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(message))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void sendButtonMessage(
            String chatId,
            String messageText,
            Map<String, URI> buttons,
            String buttonActionType
    ) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (String buttonText : buttons.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            List<String> anotherButtons = buttons.keySet().stream().filter(buttonValue -> !buttonValue.equals(buttonText)).toList();
            String callbackData = buttonActionType + " ; " + buttonText + " . " + String.join(", ", anotherButtons);
            button.setCallbackData(callbackData);
            if (buttons.get(buttonText) != null) {
                button.setUrl(buttons.get(buttonText).toString());
            }
            keyboardRows.add(List.of(button));
        }
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        message.setReplyMarkup(inlineKeyboardMarkup);
        sendTgMessage(message);
    }

    public void sendTextMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        sendTgMessage(message);
    }

    public void editMessage(Long chatId, Integer messageId, String tagToHighlight, ButtonActionTypes buttonActionType) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        UserInfo userInfo = userInfoRepository.findById(chatId).orElseThrow();
        List<String> tagWithTick = Arrays.stream(tagToHighlight.split(" ")).toList();
        String pureTag = tagWithTick.get(tagWithTick.size() - 1);
        List<String> tags = userInfo.getTags().stream()
                .map(tag -> {
                    log.info(tag);
                    if (tag.equals(pureTag)) {
                        if (tagWithTick.get(0).equals("✓")) {
                            return pureTag;
                        } else {
                            return "✓ " + tag;
                        }
                    } else {
                        return tag;
                    }
                })
                .toList();

        for (String tag : tags) {
            log.info(tag);
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(tag);
            button.setCallbackData(buttonActionType + " ; " + tag);
            keyboardRows.add(List.of(button));
        }

        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(inlineKeyboardMarkup);

        webClient.post()
                .uri("https://api.telegram.org/bot" + token + "/editMessageReplyMarkup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(editMessageReplyMarkup)
                .retrieve()
                .toBodilessEntity()
                .block();

    }

}
