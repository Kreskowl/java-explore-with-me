package ru.practicum.explorewithme.comment.service;


import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentSearchParams;
import ru.practicum.explorewithme.comment.dto.CommentTextDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(CommentTextDto dto, Long userId, Long eventId);

    CommentDto updateComment(Long userId, Long commentId, CommentTextDto dto);

    void deleteComment(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getCommentsByAdminFilters(CommentSearchParams params);

    List<CommentDto> getPublicCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> getUserComments(Long userId, Long eventId, int from, int size);

    CommentDto getAdminCommentById(Long commentId);

    CommentDto getUserCommentById(Long userId, Long commentId);
}
