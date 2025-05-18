package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.explorewithme.client.Config.ServerConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServerConfigProperties.class)
public class StatsApp {
    public static void main(String[] args) {
        SpringApplication.run(StatsApp.class, args);
    }
}
