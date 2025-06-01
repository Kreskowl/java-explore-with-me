package ru.practicum.explorewithme.mock.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.comment.controller.common.CommentPublicController;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentPublicController.class)
@AutoConfigureMockMvc
public class CommentPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService service;

    @Test
    void shouldReturnCommentsByEvent() throws Exception {
        List<CommentDto> response = List.of(
                new CommentDto(1L, "Great!", "User1", 100L, LocalDateTime.now()),
                new CommentDto(2L, "Nice!", "User2", 100L, LocalDateTime.now())
        );

        when(service.getPublicCommentsByEvent(100L, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/events/100/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Great!"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Nice!"));
    }

    @Test
    void shouldFailValidationWithNegativeFrom() throws Exception {
        mockMvc.perform(get("/events/100/comments")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}
