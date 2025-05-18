package ru.practicum.explorewithme.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.StatController;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.service.StatsService;
import ru.practicum.statsdto.ViewStats;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatController.class)
public class StatsControllerTest {
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private StatsService statsService;

    @Test
    void shouldReturnStats() throws Exception {
        List<ViewStats> result = List.of(new ViewStats("my-app", "/uri", 5L));
        when(statsService.getStats(any(), any(), any(), anyBoolean())).thenReturn(result);

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("uris", "/uri")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("my-app"));
    }

    @Test
    void shouldReturn404WhenStatsNotFound() throws Exception {
        when(statsService.getStats(any(), any(), any(), anyBoolean()))
                .thenThrow(new NotFoundException("No stats found"));

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("uris", "/invalid")
                        .param("unique", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No stats found"));
    }

    @Test
    void shouldReturn400WhenRequestParamIsWrongType() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("unique", "not-a-boolean") // ← ошибка
                        .param("uris", "/uri"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad request"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter: unique"));
    }
}
