package vacanciesalert.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HhRestClientConfig {
    private final static String API_BASE_URL = "https://api.hh.ru";

    @Bean
    public RestClient hhRestClient() {
        return RestClient.builder().baseUrl(API_BASE_URL).build();
    }
}