package ru.practicum.explorewithme.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatsControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldReturnBadRequest_whenStartAfterEnd() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-12-31 23:59:59")
                        .param("end", "2025-01-01 00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_whenUrisInvalid() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("uris", "bad_uri"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBodyIsUnreadable() throws Exception {
        String invalidJson = "{ \"app\": \"main\", \"uri\": \"/test\", \"ip\": \"127.0.0.1\", \"timestamp\": \"BAD\" }";

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request format"));
    }

    @Test
    void shouldReturn400WhenStartIsMissing() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", "2025-12-31 23:59:59"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required parameter: start"));
    }

    @Test
    void shouldNotThrow_whenUrisIsNull() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("unique", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400_whenUrisContainEmptyOrNull() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01 00:00:00")
                        .param("end", "2025-12-31 23:59:59")
                        .param("uris", "", " ", (String) null)) // key повторяется
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("'uris' must not be empty or contain blank/null values"));
    }
}
