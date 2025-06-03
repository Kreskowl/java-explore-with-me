package ru.practicum.explorewithme.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(long authorId, Pageable page);

    List<Comment> findAllByAuthorIdAndEventId(long authorId, long eventId, Pageable page);

    @Query("""
                SELECT c FROM Comment c
                WHERE (:userIds IS NULL OR c.author.id IN :userIds)
                          AND (:eventIds IS NULL OR c.event.id IN :eventIds)
                          AND (:commentIds IS NULL OR c.id IN :commentIds)
                  AND (:text IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')))
                  AND (c.createdOn BETWEEN :rangeStart AND :rangeEnd)
                  AND c.deletedByAdmin = false
            """)
    List<Comment> findByAdminFilters(
            @Param("userIds") List<Long> userIds,
            @Param("eventIds") List<Long> eventIds,
            @Param("commentIds") List<Long> commentIds,
            @Param("text") String text,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("""
                SELECT c FROM Comment c
                WHERE c.event.id = :eventId
                  AND c.deletedByAdmin = false
                ORDER BY c.createdOn DESC
            """)
    List<Comment> findByEventIdOrderByCreatedOnDesc(Long eventId);
}
