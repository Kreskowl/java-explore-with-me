package ru.practicum.explorewithme.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventSearchParams;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.SortType;
import ru.practicum.explorewithme.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.event.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.model.AdminStateAction;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.model.UserStateAction;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.exception.custom.ValidationException;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final LocalDateTime EARLIEST = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDateTime LATEST = LocalDateTime.of(3000, 1, 1, 0, 0);
    private final EventRepository repository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper mapper;
    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> searchEvents(List<Long> users,
                                           List<String> states,
                                           List<Long> categories,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           int from,
                                           int size) {

        Pageable pageable = PageRequest.of(from / size, size);

        List<EventState> stateEnums = states != null
                ? states.stream().map(EventState::valueOf).toList()
                : null;
        if (rangeStart == null) {
            rangeStart = EARLIEST;
        }
        if (rangeEnd == null) {
            rangeEnd = LATEST;
        }

        List<Event> events = repository.findEventsByAdminFilters(
                users, stateEnums, categories, rangeStart, rangeEnd, pageable);

        return events.stream()
                .map(mapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = ifEventExists(eventId);

        mapper.updateEventFromAdminRequest(request, event);
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConflictException("Admin can’t schedule event earlier than 1 hour from now");
        }

        if (request.getStateAction() != null) {
            handleAdminStateAction(event, request.getStateAction());
        }

        return mapper.toFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublishedEvents(EventSearchParams params) {
        if (params.getRangeStart() == null) {
            params.setRangeStart(EARLIEST);
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
        if (params.getSort() == SortType.VIEWS) {
            sort = Sort.by(Sort.Direction.DESC, "views");
        }

        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);

        List<Event> events = repository.findAllByPublicFilters(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                params.getOnlyAvailable(),
                RequestStatus.CONFIRMED,
                pageable
        ).getContent();

        Map<Long, String> eventUriMap = events.stream()
                .collect(Collectors.toMap(Event::getId, e -> "/events/" + e.getId()));

        Map<String, Long> views = statsClient.getViews(new ArrayList<>(eventUriMap.values()));

        events.forEach(e ->
                e.setViews(views.getOrDefault(eventUriMap.get(e.getId()), 0L))
        );

        return events.stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublishedEventById(Long id, HttpServletRequest request) {
        Event event = repository.findByIdAndEventState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        statsClient.saveHit(new StatDto(
                "ewm-main",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));

        String uri = "/events/" + id;
        Map<String, Long> views = statsClient.getViews(List.of(uri));
        event.setViews(views.getOrDefault(uri, 0L));

        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }

        Category category = ifCategoryExists(dto.getCategory());
        User user = ifUserExists(userId);

        Event event = mapper.toEntity(dto, category, user);

        return mapper.toFullDto(repository.save(event));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = repository.findByInitiatorId(userId, pageable).getContent();
        return events.stream()
                .map(mapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = repository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found for this user"));

        return mapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = repository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found for user"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can update the event");
        }
        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours in the future");
        }
        if (!(event.getEventState().equals(EventState.PENDING) || event.getEventState().equals(EventState.CANCELED))) {
            throw new ConflictException("Only pending or canceled events can be updated");
        }
        if (request.getStateAction() != null) {
            handleUserStateAction(event, request.getStateAction());
        }

        mapper.updateEventFromUserRequest(request, event);

        return mapper.toFullDto(event);
    }

    private Event ifEventExists(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id " + id + " not found"));
    }

    private User ifUserExists(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Category ifCategoryExists(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private void handleAdminStateAction(Event event, AdminStateAction action) {
        if (action == null) return;

        switch (action) {
            case PUBLISH_EVENT:
                if (!event.getEventState().equals(EventState.PENDING)) {
                    throw new ConflictException("Only pending events can be published");
                }
                event.setEventState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                if (!event.getEventState().equals(EventState.PENDING)) {
                    throw new ConflictException("Cannot reject already published event or cancelled event");
                }
                event.setEventState(EventState.CANCELED);
                break;
            default:
                throw new ValidationException("Unsupported state action: " + action);
        }
    }

    private void handleUserStateAction(Event event, UserStateAction action) {
        if (action == null) return;

        switch (action) {
            case SEND_TO_REVIEW:
                if (!event.getEventState().equals(EventState.CANCELED)) {
                    throw new ConflictException("Only canceled events can be sent to review");
                }
                event.setEventState(EventState.PENDING);
                break;
            case CANCEL_REVIEW:
                if (!event.getEventState().equals(EventState.PENDING)) {
                    throw new ConflictException("Only pending events can be canceled");
                }
                event.setEventState(EventState.CANCELED);
                break;
            default:
                throw new ValidationException("Unsupported state action: " + action);
        }
    }
}
