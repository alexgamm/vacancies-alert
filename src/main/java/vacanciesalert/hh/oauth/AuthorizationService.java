package vacanciesalert.hh.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vacanciesalert.hh.api.ApiClient;
import vacanciesalert.hh.exception.ApiException;
import vacanciesalert.hh.exception.ClientException;
import vacanciesalert.hh.oauth.model.GetTokensResponse;
import vacanciesalert.hh.oauth.model.UserTokens;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserInfoRepository userInfoRepository;

    private final ApiClient apiClient;


    public String createAuthUri(String chatId) {
        return apiClient.createAuthUri(chatId);
    }

    @Transactional
    public void authorizeUserInHh(Long chatId, String code) {
        GetTokensResponse response = apiClient.getTokens(code);
        // TODO handle exception with getting tokens
        UserInfo userInfo = UserInfo.builder()
                .chatId(chatId)
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiredAt(Instant.now().plusSeconds(response.getExpiresIn()))
                .build();
        userInfoRepository.save(userInfo);
    }

    @Transactional
    public UserTokens refreshTokens(Long chatId, String refreshToken) throws ClientException, ApiException {
        GetTokensResponse response = apiClient.refreshToken(refreshToken);
        UserTokens tokens = new UserTokens(
                response.getAccessToken(),
                response.getRefreshToken(),
                Instant.now().plusSeconds(response.getExpiresIn())
        );
        userInfoRepository.updateTokensByChatId(
                chatId,
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiration()
        );
        return tokens;
    }

//    public String getAccessToken(UserInfo user) {
//        if (user.getExpiredAt() != null && Instant.now().isBefore(user.getExpiredAt())) {
//            return user.getAccessToken();
//        }
//        return refreshTokens(user.getChatId(), user.getRefreshToken()).accessToken();
//    }

}
