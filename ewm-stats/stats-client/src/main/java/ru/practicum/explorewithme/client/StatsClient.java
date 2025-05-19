package ru.practicum.explorewithme.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.StatDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.statsdto.Constants.DATE_TIME;

@Component
public class StatsClient extends BaseClient implements StatsServiceClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME);

    public StatsClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> saveHit(StatDto dto) {
        return post("/hit", dto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           boolean unique) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", URLEncoder.encode(start.format(DATE_TIME_FORMATTER), StandardCharsets.UTF_8));
        params.put("end", URLEncoder.encode(end.format(DATE_TIME_FORMATTER), StandardCharsets.UTF_8));
        params.put("unique", unique);

        if (uris != null) {
            for (int i = 0; i < uris.size(); i++) {
                params.put("uris[" + i + "]", uris.get(i));
            }
        }

        return get("/stats", params);
    }
}
