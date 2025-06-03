package ru.practicum.explorewithme.comment.controller.auth;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.Constants;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentTextDto;
import ru.practicum.explorewithme.comment.service.CommentService;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
public class CommentPrivateController {
    private final CommentService service;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestBody @Valid CommentTextDto dto,
                                 @PathVariable Long userId,
                                 @PathVariable Long eventId) {
        return service.addComment(dto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable long userId,
                                    @PathVariable long commentId,
                                    @RequestBody @Valid CommentTextDto dto) {
        return service.updateComment(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long commentId) {
        service.deleteComment(userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable long userId, @PathVariable long commentId) {
        return service.getUserCommentById(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUsersComments(@PathVariable Long userId,
                                             @RequestParam(required = false) Long eventId,
                                             @RequestParam(required = false,
                                                     defaultValue = Constants.DEFAULT_FROM_VALUE)
                                             @PositiveOrZero int from,
                                             @RequestParam(required = false,
                                                     defaultValue = Constants.DEFAULT_SIZE_VALUE)
                                             @Positive int size) {
        return service.getUserComments(userId, eventId, from, size);
    }
}
