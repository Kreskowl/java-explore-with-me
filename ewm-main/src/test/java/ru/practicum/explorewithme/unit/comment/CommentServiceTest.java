package ru.practicum.explorewithme.unit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.CommentSearchParams;
import ru.practicum.explorewithme.comment.dto.CommentTextDto;
import ru.practicum.explorewithme.comment.model.Comment;
import ru.practicum.explorewithme.comment.repository.CommentRepository;
import ru.practicum.explorewithme.comment.service.CommentService;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.ValidationException;
import ru.practicum.explorewithme.unit.AbstractServiceTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.explorewithme.unit.TestDataFactory.createCommentDto;
import static ru.practicum.explorewithme.unit.TestDataFactory.createEvent;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommentServiceTest extends AbstractServiceTest {

    @Autowired
    private CommentService service;
    @Autowired
    private CommentRepository repository;

    @Test
    void shouldAddCommentToPublishedEvent() {
        CommentTextDto dto = createCommentDto("Some text");
        ;
        CommentDto result = service.addComment(dto, requester.getId(), event.getId());

        assertNotNull(result.getId());
        assertEquals(dto.getText(), result.getText());
        assertEquals(requester.getName(), result.getAuthorName());
        assertEquals(event.getId(), result.getEventId());
        assertNotNull(result.getCreatedOn());
    }

    @Test
    void shouldThrowWhenAddingToUnpublishedEvent() {
        Event draft = eventRepository.save(
                createEvent(initiator, category, EventState.PENDING,
                        false, 5, 0)
        );

        CommentTextDto dto = createCommentDto("Some text");
        ;

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> service.addComment(dto, requester.getId(), draft.getId())
        );

        assertTrue(ex.getMessage().contains("unpublished"));
    }

    @Test
    void shouldUpdateOwnComment() {
        CommentTextDto initial = createCommentDto("original text");
        CommentDto created = service.addComment(initial, requester.getId(), event.getId());

        CommentTextDto updated = createCommentDto("updated text");
        CommentDto result = service.updateComment(requester.getId(), created.getId(), updated);

        assertEquals("updated text", result.getText());
        assertEquals(created.getId(), result.getId());
    }

    @Test
    void shouldDeleteOwnComment() {
        CommentTextDto dto = createCommentDto("to be deleted");
        CommentDto created = service.addComment(dto, requester.getId(), event.getId());

        service.deleteComment(requester.getId(), created.getId());

        assertFalse(repository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldReturnCommentsByEvent() {
        service.addComment(createCommentDto("comment 1"), requester.getId(), event.getId());
        service.addComment(createCommentDto("comment 2"), requester.getId(), event.getId());

        List<CommentDto> comments = service.getPublicCommentsByEvent(event.getId(), 0, 10);

        assertEquals(2, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.getEventId().equals(event.getId())));
    }

    @Test
    void shouldFilterCommentsByAdminParams() {
        CommentTextDto dto1 = createCommentDto("match me");
        CommentTextDto dto2 = createCommentDto("completely different");

        CommentDto c1 = service.addComment(dto1, requester.getId(), event.getId());
        service.addComment(dto2, requester.getId(), event.getId());

        CommentSearchParams params = new CommentSearchParams();
        params.setText("match");
        params.setFrom(0);
        params.setSize(10);
        params.setSort(Sort.Direction.DESC);
        params.setRangeStart(LocalDateTime.now().minusHours(1));
        params.setRangeEnd(LocalDateTime.now().plusHours(1));

        List<CommentDto> results = service.getCommentsByAdminFilters(params);

        assertEquals(1, results.size());
        assertTrue(results.getFirst().getText().contains("match"));
    }

    @Test
    void shouldThrowWhenUpdatingOthersComment() {
        CommentDto comment = service.addComment(createCommentDto("private"), requester.getId(), event.getId());

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> service.updateComment(initiator.getId(), comment.getId(), createCommentDto("attempt"))
        );

        assertTrue(ex.getMessage().contains("only by its author"));
    }

    @Test
    void shouldThrowWhenDeletingOthersComment() {
        CommentDto comment = service.addComment(createCommentDto("not yours"), requester.getId(), event.getId());

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> service.deleteComment(initiator.getId(), comment.getId())
        );

        assertTrue(ex.getMessage().contains("only by its author"));
    }

    @Test
    void shouldThrowWhenGettingCommentsForUnpublishedEvent() {
        Event draft = eventRepository.save(
                createEvent(initiator, category, EventState.PENDING, false,
                        0, 0)
        );

        ConflictException ex = assertThrows(
                ConflictException.class,
                () -> service.getPublicCommentsByEvent(draft.getId(), 0, 10)
        );

        assertTrue(ex.getMessage().contains("published"));
    }

    @Test
    void shouldMarkCommentAsDeletedByAdmin() {
        CommentDto comment = service.addComment(createCommentDto("will be hidden"), requester.getId(), event.getId());

        service.deleteCommentByAdmin(comment.getId());

        Comment softDeleted = repository.findById(comment.getId()).orElseThrow();
        assertTrue(softDeleted.isDeletedByAdmin());
    }

    @Test
    void shouldThrowWhenRangeEndBeforeRangeStart() {
        CommentSearchParams params = new CommentSearchParams();
        params.setRangeStart(LocalDateTime.now().plusHours(2));
        params.setRangeEnd(LocalDateTime.now());

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getCommentsByAdminFilters(params)
        );

        assertTrue(ex.getMessage().contains("rangeEnd must be after rangeStart"));
    }

    @Test
    void shouldNotFailWhenRangeIsNotProvided() {
        service.addComment(createCommentDto("hello"), requester.getId(), event.getId());

        CommentSearchParams params = new CommentSearchParams();
        params.setText("hello");

        List<CommentDto> result = service.getCommentsByAdminFilters(params);

        assertEquals(1, result.size());
        assertEquals("hello", result.getFirst().getText());
    }
}
