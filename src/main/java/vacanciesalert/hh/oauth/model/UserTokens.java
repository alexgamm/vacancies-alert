package vacanciesalert.hh.oauth.model;

import java.time.Instant;

public record UserTokens(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiration
) {
}
