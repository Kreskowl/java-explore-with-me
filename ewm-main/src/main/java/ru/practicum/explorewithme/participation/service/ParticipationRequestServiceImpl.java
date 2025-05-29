package ru.practicum.explorewithme.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.ForbiddenActionException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.participation.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.participation.model.ParticipationRequest;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.participation.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository repository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper mapper;

    @Override
    public List<ParticipationRequestDto> getRequests(long userId) {
        User user = ifUserExists(userId);
        return repository.findAllByRequesterId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        ifUserExists(userId);
        ParticipationRequest request = ifRequestExists(requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new ForbiddenActionException("Request does not belong to user with id " + userId);
        }

        request.setStatus(RequestStatus.CANCELED);
        repository.save(request);

        return mapper.toDto(request);
    }

    public List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or not yours"));

        List<ParticipationRequest> requests = repository.findByEventId(eventId);
        return requests.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = getEventIfInitiator(userId, eventId);
        validateEventRequiresModeration(event);

        List<ParticipationRequest> requests = getPendingRequestsOrThrow(request.getRequestIds());

        return processStatusUpdate(request.getStatus(), requests, event);
    }

    @Override
    @Transactional
    public ParticipationRequestDto makeRequest(long userId, long eventId) {
        Event event = getPublishedEventOrThrow(eventId);
        User requester = ifUserExists(userId);

        validateNotInitiator(requester, event);
        validateEventPublished(event);
        validateNotDuplicate(eventId, userId);
        validateParticipantLimitNotReached(event);


        RequestStatus status;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        }
        ParticipationRequest request = mapper.toEntity(event, requester, status);
        request.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

        if (status == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        return mapper.toDto(repository.save(request));
    }

    private User ifUserExists(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id " + id + " not found"));
    }

    private ParticipationRequest ifRequestExists(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("request with id " + id + " not found"));
    }

    private Event getPublishedEventOrThrow(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Event with id " + eventId + " is not published");
        }

        return event;
    }

    private Event getEventIfInitiator(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only event initiator can change request statuses");
        }

        return event;
    }

    private void validateEventRequiresModeration(Event event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("No confirmation needed for this event");
        }
    }

    private List<ParticipationRequest> getPendingRequestsOrThrow(List<Long> requestIds) {
        List<ParticipationRequest> requests = repository.findAllById(requestIds);

        boolean hasNonPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);

        if (hasNonPending) {
            throw new ConflictException("All requests must be in PENDING status");
        }

        return requests;
    }

    private EventRequestStatusUpdateResult processStatusUpdate(RequestStatus status,
                                                               List<ParticipationRequest> requests,
                                                               Event event) {
        int confirmed = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();

        if (status == RequestStatus.CONFIRMED && confirmed + requests.size() > limit) {
            throw new ConflictException("Cannot confirm requests: participant limit would be exceeded");
        }

        List<ParticipationRequest> confirmedList = new ArrayList<>();
        List<ParticipationRequest> rejectedList = new ArrayList<>();

        for (ParticipationRequest r : requests) {
            if (status == RequestStatus.CONFIRMED) {
                if (confirmed < limit) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmedList.add(r);
                    confirmed++;
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    rejectedList.add(r);
                }
            } else if (status == RequestStatus.REJECTED) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedList.add(r);
            } else {
                throw new ConflictException("Unsupported status: " + status);
            }
        }

        event.setConfirmedRequests(confirmed);
        repository.saveAll(requests);

        return new EventRequestStatusUpdateResult(
                confirmedList.stream().map(mapper::toDto).toList(),
                rejectedList.stream().map(mapper::toDto).toList()
        );
    }

    private void validateNotInitiator(User requester, Event event) {
        if (event.getInitiator().getId().equals(requester.getId())) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
    }

    private void validateEventPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot request participation in unpublished event");
        }
    }

    private void validateNotDuplicate(long eventId, long userId) {
        if (repository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Duplicate request not allowed");
        }
    }

    private void validateParticipantLimitNotReached(Event event) {
        Integer limit = event.getParticipantLimit();
        if (limit != 0 && event.getConfirmedRequests() >= limit) {
            throw new ConflictException("Participant limit reached");
        }
    }
}
