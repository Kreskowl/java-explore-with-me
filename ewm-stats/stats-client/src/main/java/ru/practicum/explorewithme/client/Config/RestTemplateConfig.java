package ru.practicum.explorewithme.client.Config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explorewithme.client.StatsClient;

@Configuration
@EnableConfigurationProperties(StatsClientProperties.class)
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StatsClient statsClient(RestTemplate restTemplate, StatsClientProperties props) {
        return new StatsClient(restTemplate, props.getUrl());
    }
}
