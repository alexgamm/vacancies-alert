package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vacanciesalert.hh.api.ApiClient;
import vacanciesalert.model.hhSearchResponse.Vacancies;
import vacanciesalert.model.hhSearchResponse.Vacancy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchVacanciesService {
    private final ApiClient apiClient;
    // TODO get fresh vacancies within api query

    public List<Vacancy> getNewVacancies(String accessToken, String tag, boolean showHiddenSalaryVacancies) {
        Vacancies vacancies = apiClient.getVacancies(accessToken, tag, !showHiddenSalaryVacancies);
        return filterOnlyNewVacancies(vacancies);
    }

    public List<Vacancy> getNewVacancies(
            String accessToken,
            String tag,
            int salaryFrom,
            int salaryTo,
            boolean showHiddenSalaryVacancies
    ) {
        List<Vacancy> newVacancies = getNewVacancies(accessToken, tag, showHiddenSalaryVacancies);
        return newVacancies.stream().filter(vacancy -> {
            if (salaryTo == 0) {
                if (vacancy.getSalary().getFrom() == null) {
                    return vacancy.getSalary().getTo() >= salaryFrom;
                }
                return vacancy.getSalary().getFrom() >= salaryFrom;
            }
            if (vacancy.getSalary().getFrom() == null) {
                return vacancy.getSalary().getTo() >= salaryFrom && vacancy.getSalary().getTo() <= salaryTo;
            }
            if (vacancy.getSalary().getTo() == null) {
                return vacancy.getSalary().getFrom() >= salaryFrom && vacancy.getSalary().getFrom() <= salaryTo;
            }
            return vacancy.getSalary().getFrom() >= salaryFrom && vacancy.getSalary().getTo() <= salaryTo;
        }).toList();
    }


    private List<Vacancy> filterOnlyNewVacancies(Vacancies apiVacancies) {
        if (apiVacancies == null) {
            return Collections.emptyList();
        }
        return apiVacancies.getItems().stream()
                .filter(vacancy -> vacancy.getPublishedAt().isAfter(Instant.now().minusSeconds(600)))
                .toList();
    }

}
