package ru.practicum.explorewithme.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.StatDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.statsdto.Constants.DATE_TIME;

public class StatsClient extends BaseClient implements StatsServiceClient {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME);
    private static final LocalDateTime EARLIEST = LocalDateTime.of(2000, 1, 1, 0, 0);

    public StatsClient(RestTemplate restTemplate, String baseUrl) {
        super(restTemplate, baseUrl);
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

    private Map<String, Long> extractViews(ResponseEntity<Object> response) {
        Object body = response.getBody();
        if (!(body instanceof List<?> statsList)) return Map.of();

        return statsList.stream()
                .filter(item -> item instanceof Map)
                .map(item -> (Map<?, ?>) item)
                .collect(Collectors.toMap(
                        m -> m.get("uri").toString(),
                        m -> Long.parseLong(m.get("hits").toString())
                ));
    }

    public Map<String, Long> getViews(List<String> uris) {
        ResponseEntity<Object> response = getStats(
                EARLIEST,
                LocalDateTime.now(),
                uris,
                true
        );
        return extractViews(response);
    }
}
