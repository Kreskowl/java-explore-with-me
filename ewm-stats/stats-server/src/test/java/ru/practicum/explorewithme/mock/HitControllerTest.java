package ru.practicum.explorewithme.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.StatController;
import ru.practicum.explorewithme.service.StatsService;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatController.class)
public class HitControllerTest {
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StatsService statsService;

    @Test
    void shouldReturnBadRequest_whenAppIsBlank() throws Exception {
        StatDto dto = new StatDto(
                "",
                "/valid",
                "192.168.0.1",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenUriIsMissingSlash() throws Exception {
        StatDto dto = new StatDto(
                "ewm-main-service",
                "invalid-uri",
                "192.168.0.1",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenIpInvalid() throws Exception {
        StatDto dto = new StatDto(
                "ewm-main-service",
                "/endpoint",
                "not_an_ip",
                LocalDateTime.now()
        );

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateStatAndReturnDto() throws Exception {
        StatDto request = new StatDto("my-app", "/uri", "127.0.0.1", LocalDateTime.now());
        StatDto response = new StatDto("my-app", "/uri", "127.0.0.1", request.getTimestamp());

        when(statsService.createStat(any())).thenReturn(response);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("my-app"))
                .andExpect(jsonPath("$.uri").value("/uri"));
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        StatDto dto = new StatDto("app", "/uri", "127.0.0.1", LocalDateTime.now());

        when(statsService.createStat(any())).thenThrow(new RuntimeException("Database is down"));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error")
                        .value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.message").value("Database is down"));
    }
}
