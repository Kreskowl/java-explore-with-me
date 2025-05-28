package ru.practicum.explorewithme.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class StatsClientUnitTest {

    @Test
    void getViews_shouldReturnEmpty_whenResponseBodyIsInvalid() {
        RestTemplate mockTemplate = mock(RestTemplate.class);
        StatsClient client = spy(new StatsClient(mockTemplate, "http://localhost:9090"));

        ResponseEntity<Object> invalidResponse = ResponseEntity.ok("invalid");

        doReturn(invalidResponse).when(client).getStats(any(), any(), any(), anyBoolean());

        Map<String, Long> result = client.getViews(List.of("/event/bad"));

        assertThat(result).isEmpty();
    }

    @Test
    void getViews_shouldReturnCorrectViews_whenStatsReturnedValidList() {
        RestTemplate mockTemplate = mock(RestTemplate.class);

        Map<String, Object> item1 = Map.of("uri", "/event/1", "hits", 10L);
        Map<String, Object> item2 = Map.of("uri", "/event/2", "hits", 5L);
        List<Map<String, Object>> responseBody = List.of(item1, item2);
        ResponseEntity<Object> response = ResponseEntity.ok(responseBody);

        when(mockTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(response);

        StatsClient client = new StatsClient(mockTemplate, "http://localhost:9090");

        Map<String, Long> result = client.getViews(List.of("/event/1", "/event/2"));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "/event/1", 10L,
                "/event/2", 5L
        ));
    }
}
