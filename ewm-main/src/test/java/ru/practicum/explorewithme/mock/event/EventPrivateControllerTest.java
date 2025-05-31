package ru.practicum.explorewithme.mock.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.event.service.EventService;
import ru.practicum.explorewithme.location.dto.LocationDto;
import ru.practicum.explorewithme.participation.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.participation.service.ParticipationRequestService;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventPrivateControllerTest {
    private static final LocalDateTime CREATE_TIME = LocalDateTime.now();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private ParticipationRequestService requestService;

    @Test
    void createEvent_shouldReturn201WithFullDto() throws Exception {
        User user = userRepository.save(new User(null, "U", "u@u.com"));
        Category cat = categoryRepository.save(new Category(null, "C"));

        NewEventDto dto = createValidEventDto(cat.getId(), CREATE_TIME.plusHours(3));

        mockMvc.perform(post("/users/{userId}/events", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(dto.getTitle()))
                .andExpect(jsonPath("$.category.id").value(cat.getId()))
                .andExpect(jsonPath("$.initiator.id").value(user.getId()));
    }


    @Test
    void getUserEvents_shouldReturnListOfShortDto() throws Exception {
        User user = userRepository.save(new User(null, "L", "l@l.com"));
        Category cat = categoryRepository.save(new Category(null, "Tech"));

        eventService.createEvent(user.getId(), createValidEventDto(cat.getId(), CREATE_TIME.plusHours(3)));
        eventService.createEvent(user.getId(), createValidEventDto(cat.getId(), CREATE_TIME.plusHours(4)));

        mockMvc.perform(get("/users/{userId}/events", user.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserEventById_shouldReturnFullDto() throws Exception {
        User user = userRepository.save(new User(null, "E", "e@e.com"));
        Category cat = categoryRepository.save(new Category(null, "Music"));

        EventFullDto created = eventService.createEvent(user.getId(),
                createValidEventDto(cat.getId(), CREATE_TIME.plusHours(5)));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", user.getId(), created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.initiator.id").value(user.getId()));
    }

    @Test
    void updateUserEvent_shouldReturnUpdatedEvent() throws Exception {
        User user = userRepository.save(new User(null, "U2", "u2@u.com"));
        Category cat = categoryRepository.save(new Category(null, "PatchCat"));

        EventFullDto created = eventService.createEvent(user.getId(),
                createValidEventDto(cat.getId(), CREATE_TIME.plusDays(1)));

        UpdateEventUserRequest update = new UpdateEventUserRequest();
        update.setTitle("Updated title");
        update.setDescription("Updated desc with new description");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", user.getId(), created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated desc with new description"));
    }

    @Test
    void getRequestsForEvent_shouldReturnList() throws Exception {
        User owner = userRepository.save(new User(null, "Owner", "o@t.com"));
        User requester = userRepository.save(new User(null, "Requester", "r@t.com"));
        Category category = categoryRepository.save(new Category(null, "Category"));

        EventFullDto created = eventService.createEvent(owner.getId(),
                createValidEventDto(category.getId(), CREATE_TIME.plusHours(4)));
        Event event = eventRepository.findById(created.getId()).get();
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        requestService.makeRequest(requester.getId(), created.getId());

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", owner.getId(), created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].requester").value(requester.getId()));
    }

    @Test
    void updateRequestStatuses_shouldConfirmRequest() throws Exception {
        User owner = userRepository.save(new User(null, "Owner2", "o2@t.com"));
        User requester = userRepository.save(new User(null, "Requester2", "r2@t.com"));
        Category category = categoryRepository.save(new Category(null, "PatchCategory"));

        EventFullDto created = eventService.createEvent(owner.getId(),
                createValidEventDto(category.getId(), CREATE_TIME.plusHours(5)));
        Event event = eventRepository.findById(created.getId()).get();
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        ParticipationRequestDto req = requestService.makeRequest(requester.getId(), created.getId());

        EventRequestStatusUpdateRequest update = new EventRequestStatusUpdateRequest();
        update.setRequestIds(List.of(req.getId()));
        update.setStatus(RequestStatus.CONFIRMED);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", owner.getId(), created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(req.getId()))
                .andExpect(jsonPath("$.confirmedRequests[0].status").value("CONFIRMED"));
    }

    @Test
    void updateRequestStatuses_shouldReturn403IfNotOwner() throws Exception {
        User owner = userRepository.save(new User(null, "RealOwner", "o3@t.com"));
        User notOwner = userRepository.save(new User(null, "Hacker", "h@x.com"));
        User requester = userRepository.save(new User(null, "Requester", "r3@t.com"));
        Category category = categoryRepository.save(new Category(null, "FailCat"));

        EventFullDto created = eventService.createEvent(owner.getId(),
                createValidEventDto(category.getId(), CREATE_TIME.plusHours(3)));
        Event event = eventRepository.findById(created.getId()).get();
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        ParticipationRequestDto req = requestService.makeRequest(requester.getId(), created.getId());

        EventRequestStatusUpdateRequest update = new EventRequestStatusUpdateRequest();
        update.setRequestIds(List.of(req.getId()));
        update.setStatus(RequestStatus.CONFIRMED);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", notOwner.getId(), created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict());
    }

    private NewEventDto createValidEventDto(Long categoryId, LocalDateTime dateTime) {
        NewEventDto dto = new NewEventDto();
        dto.setTitle("Sample Event");
        dto.setAnnotation("Quick summary more than 20 symbols in annotation");
        dto.setDescription("This is a detailed description of the sample event.");
        dto.setCategory(categoryId);
        dto.setEventDate(dateTime);
        dto.setLocation(new LocationDto(50.0f, 30.0f));
        dto.setPaid(false);
        dto.setParticipantLimit(100);
        dto.setRequestModeration(true);
        return dto;
    }
}
