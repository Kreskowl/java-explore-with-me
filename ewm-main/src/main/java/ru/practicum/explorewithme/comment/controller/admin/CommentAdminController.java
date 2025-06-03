package ru.practicum.explorewithme.comment.controller.admin;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.Constants;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentSearchParams;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("admin/comments")
public class CommentAdminController {
    private final CommentService service;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long commentId) {
        service.deleteCommentByAdmin(commentId);
    }

    @GetMapping
    public List<CommentDto> getCommentsByFilters(@RequestParam(required = false) List<Long> userIds,
                                                 @RequestParam(required = false) List<Long> eventIds,
                                                 @RequestParam(required = false) List<Long> commentIds,
                                                 @RequestParam(required = false) String text,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
                                                 LocalDateTime rangeStart,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
                                                 LocalDateTime rangeEnd,
                                                 @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sort,
                                                 @RequestParam(defaultValue = Constants.DEFAULT_FROM_VALUE)
                                                 @PositiveOrZero int from,
                                                 @RequestParam(defaultValue = Constants.DEFAULT_SIZE_VALUE)
                                                 @Positive int size) {
        String safeText = text == null ? "" : text;
        CommentSearchParams params = new CommentSearchParams(
                userIds, eventIds, commentIds, safeText, rangeStart, rangeEnd, sort, from, size
        );
        return service.getCommentsByAdminFilters(params);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable long commentId) {
        return service.getAdminCommentById(commentId);
    }
}
