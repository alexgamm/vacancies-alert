package vacanciesalert.hh.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vacanciesalert.hh.oauth.model.UserTokens;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.telegram.TelegramService;

import java.time.Instant;

@Controller
@RequestMapping("/oauth/hh")
@RequiredArgsConstructor
@Slf4j
public class OauthController {

    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final TelegramService telegramService;

    @GetMapping("/redirect")
    @Transactional
    public String processAuthorization(@RequestParam("code") String code, @RequestParam("state") String chatId) {
        // TODO if state is null
        try {
            log.info("Processing authorization code {}", code);
            UserInfo userInfo = userInfoRepository.findUserInfoByChatId(Long.parseLong(chatId));
            UserTokens userTokens;
            if (userInfo.getAccessToken() == null) {
                userTokens = authorizationService.getOrRefreshTokens(code, false);
                UserTokens encryptedTokens = authorizationService.encryptTokens(userTokens);
                userInfo = new UserInfo(
                        userInfo.getChatId(),
                        encryptedTokens.accessToken(),
                        encryptedTokens.refreshToken(),
                        userTokens.accessTokenExpiration(),
                        null,
                        null
                );
                userInfoRepository.save(userInfo);
            } else {
                // TODO отправить сообщение с доступными тэгами
                telegramService.sendTextMessage(chatId, "Вы уже авторизованы. Продолжайте использовать наш телеграм-бот");
            }
        } catch (Throwable throwable) {
            return "failure-hh-oauth";
        }
        telegramService.sendTextMessage(chatId, "Поздравляю с успешной авторизацией! Последнее, " +
                "что необходимо сделать, чтобы получать уведомления о вакансиях - ввести теги для поиска " +
                "необходимых вакансий. \n/settags - нажми чтобы установить теги для поиска");
        return "success-hh-oauth";
    }
}
