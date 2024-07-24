package vacanciesalert.hh.search;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import vacanciesalert.hh.api.ApiClient;
import vacanciesalert.model.entity.UserInfo;
import vacanciesalert.model.hhSearchResponse.Vacancy;
import vacanciesalert.repository.VacancyRepository;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = {SearchVacanciesService.class, ApiClient.class, SearchVacanciesServiceTest.Config.class})
public class SearchVacanciesServiceTest {

    @Autowired
    private SearchVacanciesService searchVacanciesService;
    @Autowired
    private MockRestServiceServer mockServer;
    @Value("classpath:hh/responses/vacancies/only-with-salary-false.json")
    private Resource vacanciesWithAndWithoutSalaryFile;
    @Value("classpath:hh/responses/vacancies/only-with-salary-true.json")
    private Resource vacanciesWithSalaryFile;

    private void mockWithSalaryResponse() {
        mockServer.expect(manyTimes(), anything())
                .andExpect(queryParam("only_with_salary", String.valueOf(true)))
                .andRespond(withSuccess(vacanciesWithSalaryFile, MediaType.APPLICATION_JSON));
    }

    private void mockWithoutSalaryResponse() {
        mockServer.expect(manyTimes(), anything())
                .andExpect(queryParam("only_with_salary", String.valueOf(false)))
                .andRespond(withSuccess(vacanciesWithAndWithoutSalaryFile, MediaType.APPLICATION_JSON));
    }

    @Test
    void getNewVacancies_shouldReturnNewVacanciesWithInUserSalaryRange() {
        mockWithSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                1L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, 250000, false),
                Duration.ofDays(36500)
        );
        assertFalse(newVacancies.isEmpty());
        for (Vacancy vacancy : newVacancies) {
            assertNotNull(vacancy.getSalary(), String.format("%s salary is null", vacancy));
            assertTrue(
                    vacancy.getSalary().getFrom() == null || vacancy.getSalary().getFrom() >= 200000,
                    String.format("%s salaryFrom < 200000", vacancy)
            );
            assertTrue(
                    vacancy.getSalary().getTo() == null || vacancy.getSalary().getTo() <= 250000,
                    String.format("%s salaryTo > 250000", vacancy)
            );
        }
    }

    @Test
    void getNewVacancies_shouldReturnEmptyResult() {
        mockWithSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                2L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(230000, 230000, false),
                Duration.ofDays(36500)
        );
        assertTrue(newVacancies.isEmpty());
    }

    @Test
    void getNewVacancies_shouldReturnNewVacanciesWithInUserSalaryRangeAndWithoutSalaryAtAll() {
        mockWithoutSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                3L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(100000, 150000, true),
                Duration.ofDays(36500)
        );
        assertTrue(newVacancies.stream().anyMatch(vacancy -> vacancy.getSalary() == null));
        assertTrue(newVacancies.stream().anyMatch(vacancy -> vacancy.getSalary() != null &&
                (vacancy.getSalary().getTo() != null || vacancy.getSalary().getFrom() != null)
        ));
        for (Vacancy vacancy : newVacancies) {
            if (vacancy.getSalary() != null) {
                assertTrue(
                        vacancy.getSalary().getFrom() == null || vacancy.getSalary().getFrom() >= 100000,
                        String.format("%s salaryFrom < 100000", vacancy)
                );
                assertTrue(
                        vacancy.getSalary().getTo() == null || vacancy.getSalary().getTo() <= 150000,
                        String.format("%s salaryTo > 150000", vacancy)
                );
            }
        }
    }

    @Test
    void getNewVacancies_shouldReturnNewVacanciesWithoutHighLimit() {
        mockWithSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                4L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, null, false),
                Duration.ofDays(36500)
        );
        assertFalse(newVacancies.isEmpty());
        for (Vacancy vacancy : newVacancies) {
            assertNotNull(vacancy.getSalary(), String.format("%s salary is null", vacancy));
            assertTrue(
                    vacancy.getSalary().getFrom() == null || vacancy.getSalary().getFrom() >= 200000,
                    String.format("%s salaryFrom < 200000", vacancy)
            );
            assertTrue(
                    vacancy.getSalary().getTo() == null || vacancy.getSalary().getTo() <= 1000000000,
                    String.format("%s salaryTo > 1000000000", vacancy)
            );
        }
    }

    @Test
    void saveTheSameVacancies_shouldNotSaveAnyVacancies() {
        mockWithSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                5L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, null, false),
                Duration.ofDays(36500)
        );
        assertFalse(newVacancies.isEmpty());
        newVacancies = searchVacanciesService.getNewVacancies(
                5L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, null, false),
                Duration.ofDays(36500)
        );
        assertTrue(newVacancies.isEmpty());
    }

    @Test
    void saveNewVacanciesTwice_shouldSaveAllVacancies() {
        mockWithSalaryResponse();
        List<Vacancy> newVacancies = searchVacanciesService.getNewVacancies(
                6L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, null, false),
                Duration.ofDays(36500)
        );
        assertFalse(newVacancies.isEmpty());
        List<Vacancy> newVacanciesForAnotherUser = searchVacanciesService.getNewVacancies(
                7L,
                "Bearer ",
                "tag",
                new UserInfo.Salary(200000, null, false),
                Duration.ofDays(36500)
        );
        assertFalse(newVacancies.isEmpty());
        assertEquals(newVacancies.size(), newVacanciesForAnotherUser.size());
    }



    @Configuration
    public static class Config {
        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        MockRestServiceServer mockServer(RestClient.Builder restClientBuilder) {
            return MockRestServiceServer.bindTo(restClientBuilder).build();
        }

        @Bean
        @DependsOn("mockServer")
        RestClient hhRestClient(RestClient.Builder restClientBuilder) {
            return restClientBuilder.build();
        }

        @Bean
        VacancyRepository vacancyRepository() {
            VacancyRepository vacancyRepositoryMock = mock(VacancyRepository.class);
            List<vacanciesalert.model.entity.Vacancy> vacancies = new LinkedList<>();
            when(vacancyRepositoryMock.saveAll(any())).thenAnswer(invocation -> {
                vacancies.addAll(invocation.getArgument(0));
                return null;
            });
            when(vacancyRepositoryMock.findIds(anyLong(), any())).thenAnswer(
                    invocation -> vacancies.stream()
                            .filter(vacancy -> vacancy.getUserId() == invocation.getArgument(0, Long.class))
                            .map(vacanciesalert.model.entity.Vacancy::getVacancyId)
                            .filter(invocation.getArgument(1, Set.class)::contains)
                            .collect(Collectors.toSet())
            );
            return vacancyRepositoryMock;
        }
    }
}
