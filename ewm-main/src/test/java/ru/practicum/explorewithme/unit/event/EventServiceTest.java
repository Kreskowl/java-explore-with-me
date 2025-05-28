package ru.practicum.explorewithme.unit.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
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
import ru.practicum.explorewithme.event.service.EventService;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.location.dto.LocationDto;
import ru.practicum.explorewithme.unit.AbstractServiceTest;
import ru.practicum.explorewithme.unit.TestDataFactory;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventServiceTest extends AbstractServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper mapper;

    @MockBean
    private StatsClient statsClient;

    @Test
    void createEvent_shouldPersistAndReturnDto() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusHours(3));
        EventFullDto result = eventService.createEvent(requester.getId(), dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo(dto.getTitle());
        assertThat(result.getCategory().getId()).isEqualTo(category.getId());
        assertThat(result.getInitiator().getId()).isEqualTo(requester.getId());
    }

    @Test
    void createEvent_shouldReturnConflictExceptionIfDateLessThanTwoHoursFromNow() {
        NewEventDto dto = createValidEventDto(category.getId(), created);

        assertThatThrownBy(() -> eventService.createEvent(requester.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createEvent_shouldReturnNotFoundIfCategoryNotExist() {
        NewEventDto dto = createValidEventDto(999L, created.plusHours(3));

        assertThatThrownBy(() -> eventService.createEvent(requester.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createEvent_shouldReturnNotFoundIfUserNotExist() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusHours(3));

        assertThatThrownBy(() -> eventService.createEvent(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserEvents_shouldReturnListOfShortDtos() {
        eventRepository.save(mapper.toEntity(createValidEventDto(category.getId(),
                created.plusHours(3)), category, requester));
        eventRepository.save(mapper.toEntity(createValidEventDto(category.getId(),
                created.plusHours(5)), category, requester));

        List<EventShortDto> result = eventService.getUserEvents(requester.getId(), 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getInitiator().getId().equals(requester.getId()));
    }

    @Test
    void getUserEvents_shouldReturnUserEvents() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        eventService.createEvent(requester.getId(), dto);

        List<EventShortDto> result = eventService.getUserEvents(requester.getId(), 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo(dto.getTitle());
    }

    @Test
    void getPublishedEvents_shouldReturnEventWithStats() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        Event event = eventRepository.findById(created.getId()).orElseThrow();
        event.setEventState(EventState.PUBLISHED);
        eventRepository.save(event);

        String uri = "/events/" + event.getId();
        Mockito.when(statsClient.getViews(Mockito.argThat(list -> list.contains(uri))))
                .thenReturn(Map.of(uri, 100L));

        EventSearchParams params = setValidSearchParams(category.getId());

        List<EventShortDto> result = eventService.getPublishedEvents(params);

        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(e ->
                e.getId().equals(event.getId()) && e.getViews() == 100L
        );
    }

    @Test
    void getUserEventById_shouldReturnFullDtoForOwner() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(2));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        EventFullDto result = eventService.getUserEventById(requester.getId(), created.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getInitiator().getId()).isEqualTo(requester.getId());
    }

    @Test
    void getUserEventById_shouldThrowNotFoundIfUserIsNotOwner() {
        User stranger = userRepository.save(TestDataFactory.createUser("Stranger", "s@example.com"));
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(3));
        EventFullDto created = eventService.createEvent(initiator.getId(), dto);

        assertThatThrownBy(() -> eventService.getUserEventById(stranger.getId(), created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found for this user");
    }

    @Test
    void getUserEventById_shouldThrowNotFoundIfEventDoesNotExist() {
        assertThatThrownBy(() -> eventService.getUserEventById(requester.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found for this user");
    }

    @Test
    void updateUserEvent_shouldUpdateAndReturnFullDto() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        UpdateEventUserRequest update = new UpdateEventUserRequest();
        update.setTitle("Updated title");
        update.setAnnotation("Updated annotation");
        update.setDescription("Updated description");
        update.setEventDate(this.created.plusDays(2));

        EventFullDto result = eventService.updateUserEvent(requester.getId(), created.getId(), update);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getAnnotation()).isEqualTo("Updated annotation");
        assertThat(result.getEventDate()).isAfter(this.created.plusDays(1));
    }

    @Test
    void updateUserEvent_shouldThrowIfEventNotFound() {
        UpdateEventUserRequest update = new UpdateEventUserRequest();
        update.setTitle("Won't matter");

        assertThatThrownBy(() ->
                eventService.updateUserEvent(requester.getId(), 999L, update)
        ).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found for user");
    }

    @Test
    void updateUserEvent_shouldThrowIfEventStateIsNotPendingOrCanceled() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        Event event = eventRepository.findById(created.getId()).get();
        event.setEventState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventUserRequest update = new UpdateEventUserRequest();
        update.setTitle("Should not work");

        assertThatThrownBy(() ->
                eventService.updateUserEvent(requester.getId(), created.getId(), update)
        ).isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending or canceled events can be updated");
    }

    @Test
    void updateUserEvent_shouldThrowIfNewDateTooSoon() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        UpdateEventUserRequest update = new UpdateEventUserRequest();
        update.setEventDate(LocalDateTime.now().plusMinutes(30));

        assertThatThrownBy(() ->
                eventService.updateUserEvent(requester.getId(), created.getId(), update)
        ).isInstanceOf(ConflictException.class)
                .hasMessageContaining("Event date must be at least 2 hours in the future");
    }

    @Test
    void getPublishedEventById_shouldReturnFullDtoAndCallStats() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        Event event = eventRepository.findById(created.getId()).orElseThrow();
        event.setEventState(EventState.PUBLISHED);
        eventRepository.save(event);

        String uri = "/events/" + event.getId();
        Mockito.when(statsClient.getViews(List.of(uri)))
                .thenReturn(Map.of(uri, 42L));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        request.setRemoteAddr("127.0.0.1");

        EventFullDto result = eventService.getPublishedEventById(event.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(event.getId());
        assertThat(result.getViews()).isEqualTo(42L);

        Mockito.verify(statsClient).saveHit(Mockito.any());
        Mockito.verify(statsClient).getViews(List.of(uri));
    }

    @Test
    void updateEventByAdmin_shouldPublishPendingEvent() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(1));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        EventFullDto result = eventService.updateEventByAdmin(created.getId(), request);

        assertThat(result.getEventState()).isEqualTo(EventState.PUBLISHED);
        assertThat(result.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEventByAdmin_shouldThrowIfPublishingCanceledEvent() {
        NewEventDto dto = createValidEventDto(category.getId(), created.plusDays(2));
        EventFullDto created = eventService.createEvent(requester.getId(), dto);

        Event event = eventRepository.findById(created.getId()).orElseThrow();
        event.setEventState(EventState.CANCELED);
        eventRepository.save(event);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        assertThatThrownBy(() -> eventService.updateEventByAdmin(event.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Only pending events can be published");
    }

    @Test
    void updateEventByAdmin_shouldUpdateCategory() {
        Category newCategory = categoryRepository.save(TestDataFactory.createCategory("New"));

        EventFullDto created = eventService.createEvent(requester.getId(),
                createValidEventDto(category.getId(), this.created.plusDays(2)));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setCategory(newCategory.getId());

        EventFullDto result = eventService.updateEventByAdmin(created.getId(), request);

        assertThat(result.getCategory().getId()).isEqualTo(newCategory.getId());
    }

    @Test
    void updateEventByAdmin_shouldThrowIfCancelingPublishedEvent() {
        EventFullDto created = eventService.createEvent(requester.getId(),
                createValidEventDto(category.getId(), this.created.plusDays(2)));

        Event event = eventRepository.findById(created.getId()).orElseThrow();
        event.setEventState(EventState.PUBLISHED);
        eventRepository.save(event);

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.REJECT_EVENT);

        assertThatThrownBy(() -> eventService.updateEventByAdmin(event.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Cannot reject already published event");
    }

    @Test
    void searchEvents_shouldReturnEventsByState() {
        eventService.createEvent(requester.getId(), createValidEventDto(category.getId(), created.plusDays(1)));

        Event event = eventRepository.findAll().getFirst();
        event.setEventState(EventState.PUBLISHED);
        eventRepository.save(event);

        List<EventFullDto> result = eventService.searchEvents(
                null,
                List.of("PUBLISHED"),
                null,
                null,
                null,
                0,
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEventState()).isEqualTo(EventState.PUBLISHED);
    }

    @Test
    void searchEvents_shouldFilterByUserStateCategoryAndRange() {
        EventFullDto created = eventService.createEvent(requester.getId(),
                createValidEventDto(category.getId(), this.created.plusDays(2)));
        Event event = eventRepository.findById(created.getId()).get();
        event.setEventState(EventState.PENDING);
        eventRepository.save(event);

        List<EventFullDto> result = eventService.searchEvents(
                List.of(requester.getId()),
                List.of("PENDING"),
                List.of(category.getId()),
                this.created.minusDays(1),
                this.created.plusDays(3),
                0,
                10
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getInitiator().getId()).isEqualTo(requester.getId());
    }

    @Test
    void searchEvents_shouldThrowIfUnknownStateProvided() {
        assertThatThrownBy(() -> eventService.searchEvents(
                null,
                List.of("WRONG_STATE"),
                null,
                null,
                null,
                0,
                10
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
    }

    @Test
    void searchEvents_shouldReturnAllIfNoFiltersProvided() {
        eventService.createEvent(requester.getId(), createValidEventDto(category.getId(), created.plusDays(1)));

        List<EventFullDto> result = eventService.searchEvents(
                null, null, null, null, null, 0, 10
        );

        assertThat(result).isNotEmpty();
    }

    private NewEventDto createValidEventDto(Long categoryId, LocalDateTime dateTime) {
        NewEventDto dto = new NewEventDto();
        dto.setTitle("Sample Event");
        dto.setAnnotation("Quick summary");
        dto.setDescription("This is a detailed description of the sample event.");
        dto.setCategory(categoryId);
        dto.setEventDate(dateTime);
        dto.setLocation(new LocationDto(50.0f, 30.0f));
        dto.setPaid(false);
        dto.setParticipantLimit(100);
        dto.setRequestModeration(true);
        return dto;
    }

    private EventSearchParams setValidSearchParams(long catId) {
        EventSearchParams result = new EventSearchParams();
        result.setFrom(0);
        result.setSize(10);
        result.setSort(SortType.EVENT_DATE);
        result.setCategories(List.of(catId));
        result.setRangeStart(null);

        return result;
    }
}
