package vacanciesalert.hh.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
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

    private final WebClient webClient;

    private final static String REDIRECT_URL = "https://hh.ru/oauth/authorize";

    @Value("${hh.client.id}")
    private String clientId;

    @Value("${hh.client.secret}")
    private String clientSecret;

    @Value("${hh.redirect.uri}")
    private String redirectUri;

    public String createAuthUri(String chatId) {
        return UriComponentsBuilder.fromUriString(REDIRECT_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", chatId)
                .build()
                .toString();
    }

    // TODO split method
    public UserTokens getOrRefreshTokens(String valueToExchange, boolean refresh) {
        String tokenType = refresh ? "refresh_token" : "code";
        String grantType = refresh ? "refresh_token" : "authorization_code";
        GetTokensResponse response = webClient.post().uri("https://api.hh.ru/token", uriBuilder -> uriBuilder
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam(tokenType, valueToExchange)
                        .queryParam("grant_type", grantType)
                        .queryParam("redirect_uri", redirectUri)
                        .build())
                .retrieve()
                .bodyToMono(GetTokensResponse.class)
                .block();
        // TODO handle exception with getting tokens
        return new UserTokens(
                response.getAccessToken(),
                response.getRefreshToken(),
                Instant.now().plusSeconds(response.getExpiresIn()));
    }

    @Transactional
    public String getAccessToken(UserInfo user) {
        if (user.getExpiredAt() != null && Instant.now().isBefore(user.getExpiredAt())) {
            return user.getAccessToken();
        }
        UserTokens tokens = getOrRefreshTokens(
                user.getRefreshToken(),
                true
        );
        userInfoRepository.updateTokensByChatId(
                user.getChatId(),
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiration()
        );
        return tokens.accessToken();
    }
}
