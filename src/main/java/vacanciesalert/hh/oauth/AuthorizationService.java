package vacanciesalert.hh.oauth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import vacanciesalert.hh.oauth.model.GetTokensResponse;
import vacanciesalert.hh.oauth.model.UserTokens;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.repository.UserInfoRepository;

import java.text.MessageFormat;
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

    @Transactional
    public void authorizeUserInHh(Long chatId, String code) {
        GetTokensResponse response = webClient.post().uri(
                        "https://api.hh.ru/token", uriBuilder -> uriBuilder
                                .queryParam("client_id", clientId)
                                .queryParam("client_secret", clientSecret)
                                .queryParam("code", code)
                                .queryParam("grant_type", "authorization_code")
                                .queryParam("redirect_uri", redirectUri)
                                .build()
                )
                .retrieve()
                .bodyToMono(GetTokensResponse.class)
                .block();
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
    public UserTokens refreshTokens(Long chatId, String refreshToken) throws Exception {
        GetTokensResponse response = null;
        try {
            response = webClient.post().uri(
                            "https://api.hh.ru/token", uriBuilder -> uriBuilder
                                    .queryParam("client_id", clientId)
                                    .queryParam("client_secret", clientSecret)
                                    .queryParam("redirect_uri", redirectUri)
                                    .queryParam("grant_type", "refresh_token")
                                    .queryParam("refresh_token", refreshToken)
                                    .build()
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, apiResponse -> {
                        log.error(
                                "Api error when exchanging refresh to access token for user {}: {}",
                                chatId,
                                apiResponse);
                        return Mono.error(new Exception(": " + apiResponse));
                    })
                    .bodyToMono(GetTokensResponse.class)
                    .onErrorMap(originalException -> {
                        log.error(
                                "Invalid data for json deserialization for user {}: {}",
                                chatId,
                                originalException.getMessage()
                        );
                        return new Exception(
                                String.format("Invalid data for json deserialization for user %d", chatId),
                                originalException
                        );
                    })
                    .block();
        } catch (Throwable t) {
        }
        if (response == null) {
            throw new Exception();
        }
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
