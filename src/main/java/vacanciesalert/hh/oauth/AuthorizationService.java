package vacanciesalert.hh.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        // TODO check if salary.showhiddensalary is true by default after /start
        GetTokensResponse response = apiClient.getTokens(code);
        // TODO handle exception with getting tokens
        UserInfo userInfo = UserInfo.builder()
                .chatId(chatId)
                .tokens(new UserTokens(
                        response.getAccessToken(),
                        response.getRefreshToken(),
                        Instant.now().plusSeconds(response.getExpiresIn())
                ))
                .salary(UserInfo.Salary.builder().showHiddenSalaryVacancies(true).build())
                .isNew(true)
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
        userInfoRepository.updateTokens(chatId, tokens);
        return tokens;
    }

//    public String getAccessToken(UserInfo user) {
//        if (user.getExpiredAt() != null && Instant.now().isBefore(user.getExpiredAt())) {
//            return user.getAccessToken();
//        }
//        return refreshTokens(user.getChatId(), user.getRefreshToken()).accessToken();
//    }

}
