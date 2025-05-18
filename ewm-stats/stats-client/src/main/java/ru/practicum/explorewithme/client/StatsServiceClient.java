package ru.practicum.explorewithme.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsServiceClient {
    ResponseEntity<Object> saveHit(StatDto dto);

    ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
