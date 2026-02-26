package ru.practicum.main.stats.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.client.StatsClient;

@Configuration
public class StatsClientConfig {

    @Bean
    public RestTemplate statsRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(
            @Value("${stats-server.url}") String serverUrl,
            RestTemplate statsRestTemplate
    ) {
        return new StatsClient(serverUrl, statsRestTemplate);
    }
}