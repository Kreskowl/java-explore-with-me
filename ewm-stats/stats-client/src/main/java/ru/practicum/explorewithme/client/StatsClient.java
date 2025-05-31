package ru.practicum.explorewithme.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public ResponseEntity<List<ViewStats>> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("start", start.format(DATE_TIME_FORMATTER));
        params.add("end", end.format(DATE_TIME_FORMATTER));
        params.add("unique", String.valueOf(unique));
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                params.add("uris", uri);
            }
        }
        return get("/stats", params, new ParameterizedTypeReference<>() {
        });
    }

    public Map<String, Long> getViews(List<String> uris) {
        ResponseEntity<List<ViewStats>> response = getStats(
                EARLIEST,
                LocalDateTime.now(),
                uris,
                true
        );

        if (response.getBody() == null) {
            return Map.of();
        }

        return response.getBody().stream()
                .collect(Collectors.toMap(
                        ViewStats::getUri,
                        ViewStats::getHits,
                        Long::sum
                ));
    }
}
