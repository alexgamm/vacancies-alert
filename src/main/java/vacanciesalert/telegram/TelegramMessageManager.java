package vacanciesalert.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@RequiredArgsConstructor
abstract class TelegramMessageManager {

    @Value("${tg.bot.token}")
    private String token;

    private final WebClient webClient;


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

    abstract void sendButtonMessage(String chatId, List<InlineKeyboardButton> button);

    public void sendTextMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        sendTgMessage(message);
    }

}
