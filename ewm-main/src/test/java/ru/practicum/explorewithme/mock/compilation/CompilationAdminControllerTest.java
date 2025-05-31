package ru.practicum.explorewithme.mock.compilation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilation.service.CompilationService;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompilationAdminControllerTest {
    @MockBean
    private CompilationService service;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCompilation_shouldReturnCreatedCompilation() throws Exception {
        NewCompilationDto newDto = new NewCompilationDto();
        newDto.setTitle("Test Compilation");
        newDto.setPinned(true);
        newDto.setEvents(Set.of(1L, 2L));

        CompilationDto createdDto = new CompilationDto();
        createdDto.setId(1L);
        createdDto.setTitle("Test Compilation");
        createdDto.setPinned(true);
        createdDto.setEvents(createEventShortDto());

        Mockito.when(service.createCompilation(Mockito.any()))
                .thenReturn(createdDto);


        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.events", hasSize(2)))
                .andExpect(jsonPath("$.events[*].id", containsInAnyOrder(1, 2)));
    }

    @Test
    void deleteCompilation_shouldReturnNoContent() throws Exception {
        long compId = 1L;

        mockMvc.perform(delete("/admin/compilations/{compId}", compId))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteCompilation(compId);
    }

    @Test
    void updateCompilation_shouldReturnUpdatedDto() throws Exception {
        long compId = 1L;

        UpdateCompilationRequest updateDto = new UpdateCompilationRequest();
        updateDto.setTitle("Updated Title");
        updateDto.setPinned(false);
        updateDto.setEvents(Set.of(1L));

        CompilationDto responseDto = new CompilationDto();
        responseDto.setId(compId);
        responseDto.setTitle("Updated Title");
        responseDto.setPinned(false);
        responseDto.setEvents(createEventShortDto());

        Mockito.when(service.updateCompilation(Mockito.eq(compId), Mockito.any()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/admin/compilations/{compId}", compId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(compId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.pinned").value(false))
        ;
    }

    private Set<EventShortDto> createEventShortDto() {
        CategoryDto category = new CategoryDto(1L, "Category");
        UserShortDto initiator = new UserShortDto(1L, "User");

        EventShortDto event1 = new EventShortDto();
        event1.setId(1L);
        event1.setTitle("Title 1");
        event1.setAnnotation("Ann 1");
        event1.setCategory(category);
        event1.setConfirmedRequests(5);
        event1.setEventDate(LocalDateTime.now().plusDays(2));
        event1.setInitiator(initiator);
        event1.setPaid(false);
        event1.setViews(100L);

        EventShortDto event2 = new EventShortDto();
        event2.setId(2L);
        event2.setTitle("Title 2");
        event2.setAnnotation("Ann 2");
        event2.setCategory(category);
        event2.setConfirmedRequests(3);
        event2.setEventDate(LocalDateTime.now().plusDays(5));
        event2.setInitiator(initiator);
        event2.setPaid(true);
        event2.setViews(200L);

        return new HashSet<EventShortDto>(Set.of(event1, event2));
    }
}
