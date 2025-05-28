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
import ru.practicum.explorewithme.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.event.model.AdminStateAction;
import ru.practicum.explorewithme.event.service.EventService;
import ru.practicum.explorewithme.location.dto.LocationDto;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventAdminControllerTest {
    private static final LocalDateTime CREATE_TIME = LocalDateTime.now();
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    EventService eventService;

    @Test
    void getEvents_shouldReturn200WithFilteredResults() throws Exception {
        User user = userRepository.save(new User(null, "Admin", "admin@x.com"));
        Category cat = categoryRepository.save(new Category(null, "FilterCat"));

        eventService.createEvent(user.getId(), createValidEventDto(cat.getId(), CREATE_TIME.plusDays(1)));

        mockMvc.perform(get("/admin/events")
                        .param("users", user.getId().toString())
                        .param("states", "PENDING")
                        .param("categories", cat.getId().toString())
                        .param("rangeStart", CREATE_TIME.minusDays(1).toString())
                        .param("rangeEnd", CREATE_TIME.plusDays(2).toString())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void patchEvent_shouldReturn200AndChangeStateToPublish() throws Exception {
        User user = userRepository.save(new User(null, "Moderator", "mod@mod.com"));
        Category category = categoryRepository.save(new Category(null, "PatchTest"));

        EventFullDto created = eventService.createEvent(user.getId(),
                createValidEventDto(category.getId(), CREATE_TIME.plusDays(1)));

        UpdateEventAdminRequest request = new UpdateEventAdminRequest();
        request.setStateAction(AdminStateAction.PUBLISH_EVENT);

        mockMvc.perform(patch("/admin/events/{eventId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }

    @Test
    void patchEvent_shouldReturn400IfInvalidState() throws Exception {
        User user = userRepository.save(new User(null, "Admin", "admin@x.com"));
        Category cat = categoryRepository.save(new Category(null, "FilterCat"));
        EventFullDto created = eventService.createEvent(user.getId(),
                createValidEventDto(cat.getId(), CREATE_TIME.plusDays(1)));

        String body = "{"
                + "\"stateAction\": \"WRONG_STATE\""
                + "}";

        mockMvc.perform(patch("/admin/events/{eventId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
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
}
