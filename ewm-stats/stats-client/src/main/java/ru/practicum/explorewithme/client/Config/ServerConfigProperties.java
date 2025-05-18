package ru.practicum.explorewithme.client.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "stats-server")
public class ServerConfigProperties {
    private String url;

    public ServerConfigProperties() {
    }
}
