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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchVacanciesService {
    private final WebClient webClient;

    public Map<String, List<Vacancy>> getNewVacancies(String accessToken, Set<String> tags, Instant from) {
        Map<String, List<Vacancy>> result = new HashMap<>();
        for (String tag : tags) {
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
                result.put(tag, Collections.emptyList());
                continue;
            }
            List<Vacancy> freshVacancies = allVacancies.getItems().stream()
                    .filter(vacancy -> vacancy.getPublishedAt().isAfter(from))
                    .toList();
            result.put(tag, freshVacancies);
        }
        return result;
    }
}
