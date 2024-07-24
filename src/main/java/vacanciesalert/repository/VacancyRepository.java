package vacanciesalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vacanciesalert.model.entity.Vacancy;
import vacanciesalert.model.entity.Vacancy.VacancyId;

import java.util.Set;


@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, VacancyId> {

    @Query("select v.vacancyId from Vacancy v where v.userId = :userId and v.vacancyId in :vacanciesIds")
    Set<Long> findIds(long userId, Set<Long> vacanciesIds);
}