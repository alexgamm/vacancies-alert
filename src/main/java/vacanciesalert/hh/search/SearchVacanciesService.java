package vacanciesalert.hh.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vacanciesalert.model.hhSearchResponse.Vacancies;
import vacanciesalert.model.hhSearchResponse.Vacancy;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchVacanciesService {
    private final WebClient webClient;

    // TODO get fresh vacancies within api query

    public List<Vacancy> getNewVacancies(String accessToken, String tag, Instant from) {
        Vacancies allVacancies = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.hh.ru")
                        .path("/vacancies")
                        .queryParam("text", tag)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Vacancies.class)
                .block();
        if (allVacancies == null) {
            return Collections.emptyList();
        }
        return allVacancies.getItems().stream()
                .filter(vacancy -> vacancy.getPublishedAt().isAfter(from))
                .toList();
    }
}
