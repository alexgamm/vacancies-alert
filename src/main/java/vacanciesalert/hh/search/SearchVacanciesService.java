package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacanciesalert.hh.api.ApiClient;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.model.hh.search.SearchResponse;
import vacanciesalert.model.hh.search.Vacancy;
import vacanciesalert.repository.SentVacancyRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchVacanciesService {
    private final ApiClient apiClient;
    private final SentVacancyRepository sentVacancyRepository;

    private List<Vacancy> filterOnlyNewVacancies(List<Vacancy> vacancies, Duration cutoff) {
        return vacancies.stream()
                .filter(vacancy -> vacancy.getPublishedAt().isAfter(Instant.now().minus(cutoff)))
                .toList();
    }

    private List<Vacancy> excludePreviouslySentVacanciesIds(long userId, List<Vacancy> excludeFrom) {
        if (excludeFrom.isEmpty()) return excludeFrom;
        Map<Long, Vacancy> vacanciesByIds = excludeFrom.stream().collect(Collectors.toMap(
                vacancy -> Long.parseLong(vacancy.getId()),
                Function.identity()
        ));
        Set<Long> previouslySentVacanciesIds = sentVacancyRepository.findAbsentVacancyIds(userId, vacanciesByIds.keySet());
        log.info("New vacancies ids {}", previouslySentVacanciesIds);
        vacanciesByIds.keySet().retainAll(previouslySentVacanciesIds);
        return vacanciesByIds.values().stream().toList();
    }

    @NotNull
    private static List<Vacancy> filterVacanciesBySalary(
            List<Vacancy> newVacancies,
            boolean showHiddenSalaryVacancies,
            @NotNull Integer salaryFrom,
            @Nullable Integer salaryTo
    ) {
        List<Vacancy> list = newVacancies.stream().filter(vacancy -> {
            if (vacancy.getSalary() == null) {
                return showHiddenSalaryVacancies;
            }
            Integer apiSalaryFrom = vacancy.getSalary().getFrom();
            Integer apiSalaryTo = vacancy.getSalary().getTo();
            if (salaryTo == null) {
                if (apiSalaryFrom == null) {
                    return apiSalaryTo >= salaryFrom;
                }
                return apiSalaryFrom >= salaryFrom;
            }
            if (apiSalaryFrom == null) {
                return apiSalaryTo >= salaryFrom && apiSalaryTo <= salaryTo;
            }
            if (apiSalaryTo == null) {
                return apiSalaryFrom >= salaryFrom && apiSalaryFrom <= salaryTo;
            }
            return apiSalaryFrom >= salaryFrom && apiSalaryTo <= salaryTo;
        }).toList();
        return list;
    }

    @Transactional
    public List<Vacancy> getNewVacancies(long userId, String accessToken, String tag, UserInfo.Salary salary, Duration cutoff) {
        boolean showHiddenSalaryVacancies = salary.isShowHiddenSalaryVacancies();
        List<Vacancy> foundVacancies = ofNullable(apiClient.getVacancies(accessToken, tag, !showHiddenSalaryVacancies))
                .map(SearchResponse::getItems)
                .orElse(Collections.emptyList());
        List<Vacancy> newVacancies = excludePreviouslySentVacanciesIds(
                userId,
                filterOnlyNewVacancies(foundVacancies, cutoff)
        );
        if (salary.getFrom() != null) {
            newVacancies = filterVacanciesBySalary(
                    newVacancies,
                    showHiddenSalaryVacancies,
                    salary.getFrom(),
                    salary.getTo()
            );
        }
        sentVacancyRepository.insert(
                userId,
                newVacancies.stream()
                        .map(vacancy -> Long.parseLong(vacancy.getId()))
                        .collect(Collectors.toSet())
        );
        return newVacancies;
    }
}
