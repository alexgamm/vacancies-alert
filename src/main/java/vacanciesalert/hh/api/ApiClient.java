package vacanciesalert.hh.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import vacanciesalert.model.hhSearchResponse.Vacancies;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class ApiClient {
    private final static String API_BASE_URL = "https://api.hh.ru";
    private final WebClient webClient;

    public Vacancies getVacancies(String accessToken, String text, boolean onlyWithSalary) {
        return webClient.get()
                .uri(API_BASE_URL, uriBuilder -> uriBuilder
                        .path("/vacancies")
                        .queryParam("text", text)
                        .queryParam("date_from", LocalDate.now().toString())
                        .queryParam("only_with_salary", onlyWithSalary)
                        .build()
                )
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Vacancies.class)
                .block();
    }
}
