package ru.practicum.explorewithme.mock.participation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.participation.controller.ParticipationRequestController;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.participation.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipationRequestController.class)
public class ParticipationRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipationRequestService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRequests_shouldReturnListOfRequests() throws Exception {
        ParticipationRequestDto dto1 = new ParticipationRequestDto(1L, 1L, 1L,
                LocalDateTime.now(), RequestStatus.CONFIRMED);
        ParticipationRequestDto dto2 = new ParticipationRequestDto(2L, 1L, 2L,
                LocalDateTime.now(), RequestStatus.PENDING);

        Mockito.when(service.getRequests(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void createRequest_shouldReturnCreatedDto() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(1L, 2L, 1L,
                LocalDateTime.now(), RequestStatus.CONFIRMED);

        Mockito.when(service.makeRequest(1L, 2L)).thenReturn(dto);

        mockMvc.perform(post("/users/1/requests")
                        .param("eventId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.event").value(2))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelRequest_shouldReturnPendingStatus() throws Exception {
        ParticipationRequestDto dto = new ParticipationRequestDto(2L, 1L, 2L,
                LocalDateTime.now(), RequestStatus.PENDING);

        Mockito.when(service.cancelRequest(1L, 1L)).thenReturn(dto);

        mockMvc.perform(patch("/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
