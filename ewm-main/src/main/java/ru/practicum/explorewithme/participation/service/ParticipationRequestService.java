package ru.practicum.explorewithme.participation.service;

import ru.practicum.explorewithme.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    List<ParticipationRequestDto> getRequests(long id);

    ParticipationRequestDto makeRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
