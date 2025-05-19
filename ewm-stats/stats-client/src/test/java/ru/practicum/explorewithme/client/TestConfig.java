package ru.practicum.explorewithme.client;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.explorewithme.client.Config.ServerConfigProperties;

@SpringBootConfiguration
@ComponentScan(basePackages = "ru.practicum.explorewithme")
@EnableConfigurationProperties(ServerConfigProperties.class)
public class TestConfig {
}
