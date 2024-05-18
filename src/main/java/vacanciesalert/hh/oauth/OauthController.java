package vacanciesalert.hh.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class OauthController {

    private final UserInfoRepository userInfoRepository;
    private final AuthorizationService authorizationService;
    private final TelegramService telegramService;

    @GetMapping("/redirect")
    @Transactional
    public String processAuthorization(@RequestParam("code") String code, @RequestParam("state") String chatId) {
        // TODO if state is null
        try {
            UserInfo userInfo = userInfoRepository.findUserInfoByChatId(Long.parseLong(chatId));
            UserTokens userTokens;
            if (userInfo == null) {
                userTokens = authorizationService.getOrRefreshTokens(code, false);
                UserTokens encryptedTokens = authorizationService.encryptTokens(userTokens);
                userInfo = new UserInfo(
                        Long.parseLong(chatId),
                        encryptedTokens.accessToken(),
                        encryptedTokens.refreshToken(),
                        userTokens.accessTokenExpiration(),
                        null,
                        null
                );
                userInfoRepository.save(userInfo);
            } else if (userInfo.getExpiredAt().isBefore(Instant.now())) {
                userTokens = authorizationService.getOrRefreshTokens(
                        authorizationService.decodeToken(userInfo.getRefreshToken()),
                        true
                );
                authorizationService.updateTokens(Long.parseLong(chatId), authorizationService.encryptTokens(userTokens));
            } else {
                // TODO отправить сообщение с доступными тэгами
                telegramService.sendTextMessage(chatId, "Вы уже авторизованы. Продолжайте использовать наш телеграм-бот");
            }
        } catch (Throwable throwable) {
            return "failure-hh-oauth";
        }
        telegramService.sendTextMessage(chatId, "Поздравляем с успешной авторизацией! Последнее, " +
                "что необходимо сделать - ввести теги для поиска необходимых вакансий. \nФормат сообщения: " +
                "/tags \"преподаватель английского языка\", \"junior logistics manager\"");
        return "success-hh-oauth";
    }
}
