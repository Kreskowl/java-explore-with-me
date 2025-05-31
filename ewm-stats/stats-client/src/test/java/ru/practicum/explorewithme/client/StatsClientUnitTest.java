package ru.practicum.explorewithme.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.ViewStats;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class StatsClientUnitTest {

    @Test
    void getViews_shouldReturnEmpty_whenResponseBodyIsNull() {
        StatsClient client = spy(new StatsClient(mock(RestTemplate.class), "http://localhost:9090"));

        doReturn(ResponseEntity.ok(null))
                .when(client).getStats(any(), any(), any(), anyBoolean());

        Map<String, Long> result = client.getViews(List.of("/event/bad"));

        assertThat(result).isEmpty();
    }

    @Test
    void getViews_shouldReturnCorrectViews_whenStatsReturnedValidList() {
        RestTemplate mockTemplate = mock(RestTemplate.class);

        List<ViewStats> responseBody = List.of(
                new ViewStats("app", "/event/1", 10L),
                new ViewStats("app", "/event/2", 5L)
        );
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(responseBody);

        StatsClient client = spy(new StatsClient(mockTemplate, "http://localhost:9090"));

        doReturn(response)
                .when(client).getStats(any(), any(), any(), anyBoolean());

        Map<String, Long> result = client.getViews(List.of("/event/1", "/event/2"));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "/event/1", 10L,
                "/event/2", 5L
        ));
    }

    @Test
    void getViews_shouldMergeDuplicateUrisWithSum() {
        StatsClient client = spy(new StatsClient(mock(RestTemplate.class), "http://localhost:9090"));

        List<ViewStats> responseBody = List.of(
                new ViewStats("app", "/event/1", 5L),
                new ViewStats("app", "/event/1", 7L)
        );

        doReturn(ResponseEntity.ok(responseBody))
                .when(client).getStats(any(), any(), any(), anyBoolean());

        Map<String, Long> result = client.getViews(List.of("/event/1"));

        assertThat(result).containsEntry("/event/1", 12L);
    }
}
