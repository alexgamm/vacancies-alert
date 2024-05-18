package vacanciesalert.hh.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vacanciesalert.hh.oauth.model.GetTokensResponse;
import vacanciesalert.hh.oauth.model.UserTokens;
import vacanciesalert.repository.UserInfoRepository;
import vacanciesalert.utils.EncryptionConverter;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserInfoRepository userInfoRepository;

    private final EncryptionConverter encryptionConverter;

    private final WebClient webClient;

    @Value("${hh.client.id}")
    private String clientId;

    @Value("${hh.client.secret}")
    private String clientSecret;

    @Value("${hh.redirect.uri}")
    private String redirectUri;

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
                Instant.now().plusMillis(response.getExpiresIn()));
    }

    public UserTokens encryptTokens(UserTokens userTokens) {
        return new UserTokens(
                encryptionConverter.convertToDatabaseColumn(userTokens.accessToken()),
                encryptionConverter.convertToDatabaseColumn(userTokens.refreshToken()),
                userTokens.accessTokenExpiration()
        );
    }

    public String decodeToken(String token) {
        return encryptionConverter.convertToEntityAttribute(token);
    }

    public void updateTokens(Long chatId, UserTokens tokens) {
        userInfoRepository.updateTokensByChatId(
                chatId,
                tokens.accessToken(),
                tokens.refreshToken(),
                tokens.accessTokenExpiration()
        );
    }
}