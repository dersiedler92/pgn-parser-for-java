package io.alpa.pgnparser.lichess.config;

import io.alpa.pgnparser.lichess.api.StudiesApi;
import io.alpa.pgnparser.lichess.client.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LichessClientConfig {

    @Value("${lichess.token}")
    private String lichessToken;

    @Bean
    public ApiClient lichessApiClient() {
        var apiClient = new ApiClient();
        apiClient.setBasePath("https://lichess.org");
        apiClient.addDefaultHeader("Authorization", "Bearer " + lichessToken);
        return apiClient;
    }

    @Bean
    public StudiesApi studiesApi(
            ApiClient lichessApiClient
    ) {
        return new StudiesApi(lichessApiClient);
    }
}