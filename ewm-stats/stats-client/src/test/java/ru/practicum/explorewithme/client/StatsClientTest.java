package ru.practicum.explorewithme.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldCallGetStatsWithUris() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/a", "/b");
        boolean unique = true;

        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class), anyMap()))
                .thenReturn(response);

        statsClient.getStats(start, end, uris, unique);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class),
                argThat((Map<String, Object> map) -> map.containsKey("start") &&
                        map.containsKey("end") &&
                        map.containsKey("unique") &&
                        map.containsKey("uris[0]") &&
                        map.containsKey("uris[1]"))
        );
    }

    @Test
    void shouldCallGetStatsWithoutUris() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        boolean unique = false;

        ResponseEntity<Object> response = new ResponseEntity<>(HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class), anyMap()))
                .thenReturn(response);

        statsClient.getStats(start, end, null, unique);

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(),
                eq(Object.class),
                argThat((Map<String, Object> map) ->
                        map.containsKey("start") &&
                                map.containsKey("end") &&
                                map.containsKey("unique") &&
                                map.keySet().stream().noneMatch(k -> k.startsWith("uris["))
                )
        );
    }
}
