package ru.practicum.explorewithme.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentSearchParams;
import ru.practicum.explorewithme.comment.dto.CommentTextDto;
import ru.practicum.explorewithme.comment.mapper.CommentMapper;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.repository.CommentRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.exception.custom.ValidationException;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private static final LocalDateTime LATEST = LocalDateTime.of(3000, 1, 1, 0, 0);
    private static final Integer DEFAULT_OFFSET_SECONDS = 10;
    private final CommentRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper mapper;

    @Override
    @Transactional
    public CommentDto addComment(CommentTextDto dto, Long userId, Long eventId) {
        User user = ifUserExists(userId);
        Event event = ifEventExists(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot add comment to unpublished event");
        }

        Comment comment = repository.save(mapper.toEntity(dto, event, user));

        return mapper.toDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, CommentTextDto dto) {
        ifUserExists(userId);
        Comment comment = ifCommentExists(commentId);

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("comments can be updated only by its author");
        }

        mapper.updateCommentFromDto(dto, comment);

        return mapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        ifUserExists(userId);
        Comment comment = ifCommentExists(commentId);

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("comments can be deleted only by its author");
        }
        repository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getUserComments(Long authorId, Long eventId, int from, int size) {
        ifUserExists(authorId);

        PageRequest page = PageRequest.of(from / size, size);

        List<Comment> comments;

        if (eventId != null) {
            ifEventExists(eventId);
            comments = repository.findAllByAuthorIdAndEventId(authorId, eventId, page)
                    .stream()
                    .toList();
        } else {
            comments = repository.findAllByAuthorId(authorId, page)
                    .stream()
                    .toList();
        }

        return comments.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CommentDto getAdminCommentById(Long commentId) {
        Comment comment = ifCommentExists(commentId);

        return mapper.toDto(comment);
    }

    @Override
    public CommentDto getUserCommentById(Long userId, Long commentId) {
        ifUserExists(userId);
        Comment comment = ifCommentExists(commentId);

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("Only the author can view this comment");
        }

        return mapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getPublicCommentsByEvent(Long eventId, int from, int size) {
        Event event = ifEventExists(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Comments are available only for published events");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        return repository.findByEventIdOrderByCreatedOnDesc(eventId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = ifCommentExists(commentId);
        comment.setDeletedByAdmin(true);
        repository.save(comment);
    }

    @Override
    public List<CommentDto> getCommentsByAdminFilters(CommentSearchParams params) {
        normalizeDateRange(params);
        validateRange(params);

        Sort.Direction direction = params.getSort();
        Sort sort = Sort.by(direction, "createdOn");

        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                sort
        );

        List<Comment> comments = repository.findByAdminFilters(
                params.getUserIds(),
                params.getEventIds(),
                params.getCommentIds(),
                params.getText(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable
        );

        return comments.stream()
                .map(mapper::toDto)
                .toList();
    }

    private User ifUserExists(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id " + id + " not found"));
    }

    private Event ifEventExists(long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id " + id + " not found"));
    }

    private Comment ifCommentExists(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("comment with id " + id + " not found"));
    }

    private void normalizeDateRange(CommentSearchParams params) {
        if (params.getRangeStart() == null) {
            params.setRangeStart(LocalDateTime.now().minusSeconds(DEFAULT_OFFSET_SECONDS));
        }
        if (params.getRangeEnd() == null) {
            params.setRangeEnd(LATEST);
        }
    }

    private void validateRange(CommentSearchParams params) {
        if (params.getRangeEnd().isBefore(params.getRangeStart())) {
            throw new ValidationException("rangeEnd must be after rangeStart");
        }
    }
}
