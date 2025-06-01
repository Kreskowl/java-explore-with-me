package ru.practicum.explorewithme.mock.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.comment.controller.admin.CommentAdminController;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentAdminController.class)
@AutoConfigureMockMvc
class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Test
    void shouldDeleteCommentByAdminSuccessfully() throws Exception {
        mockMvc.perform(delete("/admin/comments/{commentId}", 1L))
                .andExpect(status().isNoContent());

        verify(commentService).deleteCommentByAdmin(1L);
    }

    @Test
    void shouldReturnFilteredComments() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("test");
        dto.setAuthorName("admin");
        dto.setEventId(100L);
        dto.setCreatedOn(LocalDateTime.now());

        when(commentService.getCommentsByAdminFilters(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/comments")
                        .param("userIds", "1")
                        .param("sort", "DESC")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(dto.getId()))
                .andExpect(jsonPath("$[0].text").value(dto.getText()));
    }
}
