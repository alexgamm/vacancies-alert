package vacanciesalert.hh.api;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import vacanciesalert.hh.exception.ApiException;
import vacanciesalert.hh.exception.ClientException;
import vacanciesalert.hh.oauth.model.GetTokensResponse;
import vacanciesalert.model.hh.search.SearchResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class ApiClient {
    private final RestClient hhRestClient;

    @Value("${hh.client.id}")
    private String clientId;

    @Value("${hh.client.secret}")
    private String clientSecret;

    @Value("${hh.redirect.uri}")
    private String redirectUri;

    private final static String REDIRECT_URL = "https://hh.ru/oauth/authorize";

    public String createAuthUri(String chatId) {
        return UriComponentsBuilder.fromUriString(REDIRECT_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", chatId)
                .build()
                .toString();
    }

    public GetTokensResponse getTokens(String code) {
        // TODO handle error
        return hhRestClient.post().uri(
                        "https://api.hh.ru/token", uriBuilder -> uriBuilder
                                .queryParam("client_id", clientId)
                                .queryParam("client_secret", clientSecret)
                                .queryParam("code", code)
                                .queryParam("grant_type", "authorization_code")
                                .queryParam("redirect_uri", redirectUri)
                                .build()
                )
                .retrieve()
                .body(GetTokensResponse.class);
    }

    // TODO handle error
    public SearchResponse getVacancies(String accessToken, String text, boolean onlyWithSalary) {
        return hhRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vacancies")
                        .queryParam("text", text)
                        .queryParam("date_from", LocalDate.now().toString())
                        .queryParam("only_with_salary", onlyWithSalary)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(SearchResponse.class);
    }

    @NotNull
    public GetTokensResponse refreshToken(String refreshToken) throws ClientException, ApiException {
        GetTokensResponse response;
        try {
            response = hhRestClient.post().uri(
                            uriBuilder -> uriBuilder
                                    .path("/token")
                                    .queryParam("client_id", clientId)
                                    .queryParam("client_secret", clientSecret)
                                    .queryParam("redirect_uri", redirectUri)
                                    .queryParam("grant_type", "refresh_token")
                                    .queryParam("refresh_token", refreshToken)
                                    .build()
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, ApiClient::handleError)
                    .body(GetTokensResponse.class);
        } catch (Throwable t) {
            throw new ClientException(t);
        }
        if (response == null) {
            throw new ClientException(new IllegalStateException("Response is null"));
        }
        return response;
    }

    @SneakyThrows
    private static void handleError(HttpRequest req, ClientHttpResponse resp) {
        try {
            throw new ApiException(resp.getStatusCode(), IOUtils.toString(resp.getBody(), StandardCharsets.UTF_8));
        } catch (Throwable e) {
            if (e instanceof ApiException) {
                throw e;
            }
            throw new ClientException(e);
        }
    }
}
