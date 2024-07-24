package vacanciesalert.config;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import vacanciesalert.converter.UserTokenConverters;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig extends AbstractJdbcConfiguration {

    private final UserTokenConverters.Reading userTokensReadingConverter;
    private final UserTokenConverters.Writing userTokensWritingConverter;

    @NotNull
    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(List.of(
                userTokensReadingConverter,
                userTokensWritingConverter
        ));
    }
}
