package ru.practicum.explorewithme;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.StatsService;
import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.statsdto.Constants.DATE_TIME;

@RequiredArgsConstructor
@Validated
@RestController
public class StatController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatDto saveHit(@RequestBody @Valid StatDto dto) {
        return service.createStat(dto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam(name = "start")
                                    @DateTimeFormat(pattern = DATE_TIME) LocalDateTime start,
                                    @RequestParam(name = "end")
                                    @DateTimeFormat(pattern = DATE_TIME) LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
