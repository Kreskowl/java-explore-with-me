package ru.practicum.explorewithme.mock.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.event.service.EventService;
import ru.practicum.explorewithme.location.dto.LocationDto;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventPublicControllerTest {
    private static final LocalDateTime CREATE_TIME = LocalDateTime.now();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventService eventService;
    @MockBean
    private StatsClient statsClient;

    @Test
    void getEvents_shouldReturnFilteredPublishedList() throws Exception {
        User user = userRepository
                .save(new User(null, "User", "user@x.com"));
        Category cat = categoryRepository.save(new Category(null, "Public"));

        EventFullDto created = eventService.createEvent(user.getId(),
                createValidEventDto(cat.getId(), CREATE_TIME.plusHours(6)));

        Event event = eventRepository.findById(created.getId()).get();
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        mockMvc.perform(get("/events")
                        .param("text", "Sample")
                        .param("categories", cat.getId().toString())
                        .param("paid", "false")
                        .param("rangeStart", CREATE_TIME.minusDays(1).toString())
                        .param("rangeEnd", CREATE_TIME.plusDays(3).toString())
                        .param("onlyAvailable", "false")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(created.getId()));
    }

    @Test
    void getEventById_shouldReturnEventAndSaveHit() throws Exception {
        User user = userRepository.save(new User(null, "PublicUser", "public@user.com"));
        Category cat = categoryRepository.save(new Category(null, "SingleEvent"));

        EventFullDto created = eventService
                .createEvent(user.getId(), createValidEventDto(cat.getId(), CREATE_TIME.plusDays(1)));

        Event event = eventRepository.findById(created.getId()).get();
        event.setState(EventState.PUBLISHED);
        eventRepository.save(event);

        Mockito.when(statsClient.getViews(List.of("/events/" + event.getId())))
                .thenReturn(Map.of("/events/" + event.getId(), 99L));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/events/" + event.getId());
        request.setRemoteAddr("127.0.0.1");

        EventFullDto result = eventService.getPublishedEventById(event.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.getViews()).isEqualTo(99L);
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
