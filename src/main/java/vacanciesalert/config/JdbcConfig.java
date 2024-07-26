package vacanciesalert.config;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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

    @Bean
    public SimpleJdbcInsert sentVacancyInsert(JdbcTemplate jdbcTemplate){
        var insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("sent_vacancy")
                .usingColumns("vacancy_id", "user_id");
        insert.compile();
        return insert;
    }
}
