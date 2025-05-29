package ru.practicum.explorewithme.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
class StatsClientTest {

    @Autowired
    private StatsClient statsClient;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldCallSaveHit() {
        StatDto dto = new StatDto("app", "/uri", "127.0.0.1", LocalDateTime.now());
        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.CREATED);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                eq(Object.class))
        ).thenReturn(response);

        statsClient.saveHit(dto);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void shouldCallGetStatsWithCorrectParams() {
        LocalDateTime start = LocalDateTime.of(2025, 5, 28, 9, 30, 23);
        LocalDateTime end = LocalDateTime.of(2025, 5, 29, 9, 30, 23);
        List<String> uris = List.of("/a", "/b");
        boolean unique = true;

        RestTemplate restTemplate = mock(RestTemplate.class);
        StatsClient statsClient = new StatsClient(restTemplate, "http://localhost:9090");

        @SuppressWarnings("unchecked")
        ResponseEntity<Object> response = ResponseEntity.ok(Collections.emptyList());

        when(restTemplate.exchange(
                eq("http://localhost:9090/stats"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class),
                anyMap()
        )).thenReturn(response);

        statsClient.getStats(start, end, uris, unique);


        verify(restTemplate).exchange(
                eq("http://localhost:9090/stats"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class),
                argThat((Map<String, ?> map) ->
                        ((List<String>) map.get("start")).get(0).equals(start
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) &&
                                ((List<String>) map.get("end")).get(0).equals(end
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))) &&
                                ((List<String>) map.get("unique")).get(0).equals("true") &&
                                ((List<String>) map.get("uris")).equals(uris)
                )
        );
    }

    @Test
    void shouldCallGetStatsWithoutUris() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        StatsClient statsClient = new StatsClient(restTemplate, "http://localhost:9090");

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        boolean unique = false;

        @SuppressWarnings("unchecked")
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(Collections.emptyList());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramCaptor = ArgumentCaptor.forClass(Map.class);

        when(restTemplate.exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                any(),
                any(ParameterizedTypeReference.class),
                paramCaptor.capture()
        )).thenReturn(response);

        statsClient.getStats(start, end, null, unique);

        String capturedUrl = urlCaptor.getValue();
        Map<String, Object> params = paramCaptor.getValue();

        assertThat(capturedUrl).isEqualTo("http://localhost:9090/stats");
        assertThat(params).containsKeys("start", "end", "unique");
        assertThat(params).doesNotContainKey("uris");

        assertThat(params.get("unique")).isEqualTo(List.of("false"));
    }
}
