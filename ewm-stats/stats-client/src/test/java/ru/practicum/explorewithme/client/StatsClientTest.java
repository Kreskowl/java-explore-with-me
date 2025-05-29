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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
}
