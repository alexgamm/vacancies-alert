package vacanciesalert.hh.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

@Controller
@RequestMapping("/oauth/hh")
@RequiredArgsConstructor
@Slf4j
public class OauthController {

    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final TelegramService telegramService;

    @GetMapping("/redirect")
    public String processAuthorization(@RequestParam("code") String code, @RequestParam("state") String chatIdStr) {
        // TODO if state is null
        long chatId = Long.parseLong(chatIdStr);
        try {
            log.info("Processing authorization code {}", code);
            UserInfo userInfo = userInfoRepository.findById(chatId).orElse(null);
            if (userInfo == null) {
                authorizationService.authorizeUserInHh(chatId, code);
            } else {
                // TODO отправить сообщение с доступными тэгами
                telegramService.sendTextMessage(chatId, "Вы уже авторизованы. Продолжайте использовать наш телеграм-бот");
            }
        } catch (Throwable ex) {
            log.error("Could not auth", ex);
            return "failure-hh-oauth";
        }
        telegramService.sendTextMessage(chatId, "Поздравляю с успешной авторизацией! Последнее, " +
                "что необходимо сделать, чтобы получать уведомления о вакансиях - ввести теги для поиска " +
                "необходимых вакансий. \n/settags - нажми чтобы установить теги для поиска");
        return "success-hh-oauth";
    }
}
