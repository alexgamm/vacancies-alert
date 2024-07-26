package vacanciesalert.repository;

import java.util.Set;

public interface SentVacancyRepository {
    void insert(long userId, Set<Long> ids);

    Set<Long> findAbsentVacancyIds(long userId, Set<Long> ids);
}
