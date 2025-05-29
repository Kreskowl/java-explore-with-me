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
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventSearchParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.SortType;
import ru.practicum.explorewithme.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/events")
public class EventPublicController {
    private final EventService service;

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
        String safeText = text == null ? "" : text;
        EventSearchParams params = new EventSearchParams(
                safeText, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size
        );
        return service.getPublishedEvents(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable long id,
                                     HttpServletRequest request) {
        return service.getPublishedEventById(id, request);
    }
}
