package vacanciesalert.repository;

import java.util.Set;

public interface VacancyRepository {
    void insert(long userId, Set<Long> ids);

    Set<Long> findIds(long userId, Set<Long> ids);
}
