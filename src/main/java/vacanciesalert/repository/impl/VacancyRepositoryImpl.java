package vacanciesalert.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vacanciesalert.repository.VacancyRepository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class VacancyRepositoryImpl implements VacancyRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insert(long userId, Set<Long> ids) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO vacancy (user_id, vacancy_id) VALUES (?, ?)",
                ids,
                100,
                (ps, id) -> {
                    ps.setLong(1, userId);
                    ps.setLong(2, id);
                }
        );
    }

    @Override
    public Set<Long> findIds(long userId, Set<Long> ids) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userId", userId);
        parameters.addValue("ids", ids);

        return new HashSet<>(namedParameterJdbcTemplate.query(
                "SELECT vacancy_id FROM vacancy WHERE user_id = :userId AND vacancy_id IN (:ids)",
                parameters,
                (rs, rowNum) -> rs.getLong("vacancy_id")
        ));
    }
}
