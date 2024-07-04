package vacanciesalert.hh.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import vacanciesalert.hh.api.ApiClient;

@ExtendWith(MockitoExtension.class)
public class SearchVacanciesServiceTest {
    @Mock
    private ApiClient apiClient;
    @InjectMocks
    private SearchVacanciesService searchVacanciesService;

//    @Test
//    void
}
