package ru.practicum.explorewithme.mock.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.comment.controller.auth.CommentPrivateController;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentTextDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentPrivateController.class)
@AutoConfigureMockMvc
public class CommentPrivateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAddCommentSuccessfully() throws Exception {
        CommentTextDto dto = new CommentTextDto("Great event!");
        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText(dto.getText());
        responseDto.setAuthorName("User1");
        responseDto.setEventId(100L);
        responseDto.setCreatedOn(LocalDateTime.now());

        when(service.addComment(any(CommentTextDto.class), eq(1L), eq(100L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/users/1/comments/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great event!"))
                .andExpect(jsonPath("$.authorName").value("User1"))
                .andExpect(jsonPath("$.eventId").value(100));
    }

    @Test
    void shouldFailWhenTextIsBlank() throws Exception {
        CommentTextDto dto = new CommentTextDto(" ");

        mockMvc.perform(post("/users/1/comments/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldUpdateCommentSuccessfully() throws Exception {
        CommentTextDto dto = new CommentTextDto("Updated text!");
        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText("Updated text!");
        responseDto.setAuthorName("User1");
        responseDto.setEventId(100L);
        responseDto.setCreatedOn(LocalDateTime.now());

        when(service.updateComment(eq(1L), eq(1L), any(CommentTextDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Updated text!"))
                .andExpect(jsonPath("$.authorName").value("User1"))
                .andExpect(jsonPath("$.eventId").value(100));
    }

    @Test
    void shouldFailValidationWhenTextIsBlank() throws Exception {
        CommentTextDto invalidDto = new CommentTextDto(" ");

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteCommentSuccessfully() throws Exception {
        mockMvc.perform(delete("/users/1/comments/1"))
                .andExpect(status().isNoContent());

        verify(service).deleteComment(1L, 1L);
    }
}
