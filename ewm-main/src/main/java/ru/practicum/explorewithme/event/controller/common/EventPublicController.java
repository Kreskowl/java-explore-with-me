package ru.practicum.explorewithme.event.controller.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventSearchParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.service.EventService;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/events")
public class EventPublicController {
    private final EventService service;

    @GetMapping
    public List<EventShortDto> getEvents(EventSearchParams params) {
        return service.getPublishedEvents(params);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable long id,
                                     HttpServletRequest request) {
        return service.getPublishedEventById(id, request);
    }
}
