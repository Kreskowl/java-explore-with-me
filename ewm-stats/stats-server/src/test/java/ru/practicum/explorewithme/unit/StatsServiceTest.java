package ru.practicum.explorewithme.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.entity.Stat;
import ru.practicum.explorewithme.exception.InvalidUriParameterException;
import ru.practicum.explorewithme.exception.TimeRangeValidationException;
import ru.practicum.explorewithme.service.StatsRepository;
import ru.practicum.explorewithme.service.StatsService;
import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StatsServiceTest {

    private static final LocalDateTime start = LocalDateTime.now();
    private static final LocalDateTime end = LocalDateTime.now().plusHours(1);
    @Autowired
    private StatsService statsService;
    @Autowired
    private StatsRepository repository;

    @Test
    void createStat_shouldPersistAndReturnDto() {
        StatDto dto = new StatDto();
        dto.setApp("test-app");
        dto.setUri("/test");
        dto.setIp("192.168.0.1");
        dto.setTimestamp(start);

        StatDto saved = statsService.createStat(dto);

        assertThat(saved.getApp()).isEqualTo("test-app");
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void getStats_shouldReturnNonUniqueCounts() {
        repository.save(new Stat(null, "app", "/url", "ip1", start.minusHours(1)));
        repository.save(new Stat(null, "app", "/url", "ip2", start.minusMinutes(10)));

        List<ViewStats> result = statsService.getStats(
                start.minusHours(2), start.plusHours(1), List.of("/url"), false
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getHits()).isEqualTo(2);
    }

    @Test
    void getStats_shouldReturnUniqueCounts() {
        repository.save(new Stat(null, "app", "/url", "ip1", start.minusHours(1)));
        repository.save(new Stat(null, "app", "/url", "ip1", start.minusMinutes(10)));

        List<ViewStats> result = statsService.getStats(
                start.minusHours(2), start.plusHours(1), List.of("/url"), true
        );

        assertThat(result.getFirst().getHits()).isEqualTo(1);
    }

    @Test
    void getStats_whenUrisEmpty_shouldThrowInvalidUriParameterException() {
        List<String> uris = List.of();
        assertThrows(InvalidUriParameterException.class, () ->
                statsService.getStats(start, end, uris, false));
    }

    @Test
    void getStats_whenUrisContainBlank_shouldThrowInvalidUriParameterException() {
        List<String> uris = new ArrayList<>();
        uris.add(" ");
        uris.add(null);
        uris.add("");
        assertThrows(InvalidUriParameterException.class, () ->
                statsService.getStats(start, end, uris, false));
    }

    @Test
    void getStats_whenUrisHaveInvalidFormat_shouldThrowInvalidUriParameterException() {
        List<String> uris = List.of("events.1");
        assertThrows(InvalidUriParameterException.class, () ->
                statsService.getStats(start, end, uris, false));
    }

    @Test
    void getStats_whenStartAfterEnd_shouldThrowTimeRangeValidationException() {
        List<String> uris = List.of("/test");
        assertThrows(TimeRangeValidationException.class, () ->
                statsService.getStats(start.plusDays(1), end, uris, false));
    }
}

