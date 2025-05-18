package ru.practicum.explorewithme.client.Config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(ServerConfigProperties.class)
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ServerConfigProperties config, RestTemplateBuilder builder) {
        return builder.rootUri(config.getUrl()).build();
    }
}
