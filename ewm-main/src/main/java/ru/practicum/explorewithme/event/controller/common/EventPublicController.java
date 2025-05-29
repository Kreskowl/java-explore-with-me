package ru.practicum.explorewithme.event.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventSearchParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.SortType;
import ru.practicum.explorewithme.event.service.EventService;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/events")
public class EventPublicController {
    private final EventService service;
    private final StatsClient statsClient;

    @GetMapping()
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false, defaultValue = "EVENT_DATE") SortType sort,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        EventSearchParams params = new EventSearchParams(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size
        );
        statsClient.saveHit(buildHit(request));
        return service.getPublishedEvents(params);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable long id,
                                     HttpServletRequest request) {
        statsClient.saveHit(buildHit(request));
        return service.getPublishedEventById(id, request);
    }

    private StatDto buildHit(HttpServletRequest request) {
        return new StatDto(
                "ewm-main",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
    }
}
