package ru.practicum.explorewithme.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explorewithme.StatsApp;
import ru.practicum.explorewithme.client.Config.RestTemplateConfig;
import ru.practicum.explorewithme.client.Config.ServerConfigProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = {StatsApp.class, RestTemplateConfig.class},
        properties = "stats-server.url=http://localhost:9090"
)
@EnableConfigurationProperties(ServerConfigProperties.class)
class ConfigTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ServerConfigProperties config;

    @Test
    void shouldLoadRestTemplateBean() {
        assertNotNull(restTemplate);
    }

    @Test
    void shouldLoadConfig() {
        ServerConfigProperties props = new ServerConfigProperties();
        props.setUrl("http://test");
        assertEquals("http://test", props.getUrl());
    }
}
