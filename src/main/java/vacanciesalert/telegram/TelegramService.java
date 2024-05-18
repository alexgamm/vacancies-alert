package vacanciesalert.telegram;

import jakarta.ws.rs.core.UriBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramService {
    private final static String REDIRECT_URL = "https://hh.ru/oauth/authorize";

    private final WebClient webClient;

    @Value("${tg.bot.token}")
    private String token;

    @Value("${hh.client.id}")
    private String clientId;

    @Value("${hh.redirect.uri}")
    private String redirectUri;


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

    public void sendButtonMessage(String chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Авторизация на hh");
        URI uri = UriBuilder.fromUri(REDIRECT_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", chatId)
                .build();
        button.setUrl(uri.toString());
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Чтобы искать вакансии для вас, необходимо перейти по кнопке ниже");
        message.setReplyMarkup(inlineKeyboardMarkup);
        sendTgMessage(message);
    }

    public void sendTextMessage(String chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        sendTgMessage(message);
    }

}
