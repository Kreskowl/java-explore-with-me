package ru.practicum.explorewithme.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.service.ParticipationRequestService;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/users/{userId}/requests")
public class ParticipationRequestController {
    private final ParticipationRequestService service;

    @GetMapping()
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {
        return service.getRequests(userId);
    }

    @PostMapping()
    public ParticipationRequestDto createRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId) {
        return service.makeRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable long userId, @PathVariable long requestId) {
        return service.cancelRequest(userId, requestId);
    }
}
