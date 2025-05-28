package ru.practicum.explorewithme.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stats-server")
@Getter
@Setter
public class StatsServerProperties {
    private String url;
}
