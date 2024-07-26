package vacanciesalert.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import vacanciesalert.repository.SentVacancyRepository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SentVacancyRepositoryImpl implements SentVacancyRepository {
    private final JdbcClient jdbcClient;
    private final SimpleJdbcInsert sentVacancyInsert;

    @Override
    public void insert(long userId, Set<Long> ids) {
        sentVacancyInsert.executeBatch(
                ids.stream()
                        .map(id -> new MapSqlParameterSource()
                                .addValue("user_id", userId)
                                .addValue("vacancy_id", id)
                        )
                        .toArray(MapSqlParameterSource[]::new)
        );
    }

    @Override
    public Set<Long> findAbsentVacancyIds(long userId, Set<Long> ids) {
        return jdbcClient
                .sql("""
                        SELECT vacancy_id
                        FROM unnest(:ids) AS vacancy_id
                        WHERE vacancy_id NOT IN (
                            SELECT vacancy_id
                            FROM sent_vacancy
                            WHERE user_id = :userId
                        )"""
                )
                .param("userId", userId)
                .param("ids", ids.toArray(Long[]::new))
                .query(Long.class)
                .set();
    }
}
