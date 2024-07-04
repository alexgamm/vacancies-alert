package vacanciesalert.hh.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import vacanciesalert.hh.api.ApiClient;
import vacanciesalert.model.hhSearchResponse.Vacancy;

import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = {SearchVacanciesService.class, ApiClient.class, SearchVacanciesServiceTest.Config.class})
public class SearchVacanciesServiceTest {
    @Autowired
    private SearchVacanciesService searchVacanciesService;
    @Autowired
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    @Value("classpath:hh/responses/vacancies/only-with-salary-false.json")
    private Resource vacanciesWithAndWithoutSalaryFile;
    @Value("classpath:hh/responses/vacancies/only-with-salary-true.json")
    private Resource vacanciesWithSalaryFile;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer.expect(requestTo("/vacancies"))
                .andExpect(queryParam("only_with_salary", String.valueOf(true)))
                .andRespond(withSuccess(vacanciesWithSalaryFile, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("/vacancies"))
                .andExpect(queryParam("only_with_salary", String.valueOf(false)))
                .andRespond(withSuccess(vacanciesWithAndWithoutSalaryFile, MediaType.APPLICATION_JSON));
    }

    @Test
    void getNewVacancies_shouldReturnNewVacanciesWithInUserSalaryRange() {
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies("token", "tag", 50000, 60000, false);

    }

    @Configuration
    public static class Config {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        RestClient hhRestClient(RestClient.Builder restClientBuilder) {
            return restClientBuilder.build();
        }
    }
}
