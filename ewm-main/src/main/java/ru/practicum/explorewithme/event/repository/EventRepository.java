package ru.practicum.explorewithme.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.participation.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByIdAndState(Long id, EventState state);

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
                SELECT e FROM Event e
                WHERE (:users IS NULL OR e.initiator.id IN :users)
                  AND (:states IS NULL OR e.state IN :states)
                  AND (:categories IS NULL OR e.category.id IN :categories)
                  AND e.eventDate >= :rangeStart
                  AND e.eventDate <= :rangeEnd
            """)
    List<Event> findEventsByAdminFilters(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime start,
            @Param("rangeEnd") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Event e
            WHERE e.state = 'PUBLISHED'
              AND (:text IS NULL OR
                   LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR
                   LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')))
              AND (:categories IS NULL OR e.category.id IN :categories)
              AND (:paid IS NULL OR e.paid = :paid)
              AND (e.eventDate >= :rangeStart)
              AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
              AND (:onlyAvailable = false OR e.participantLimit > (
                    SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event.id = e.id AND r.status = :confirmed
              ))
            """)
    Page<Event> findAllByPublicFilters(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") boolean onlyAvailable,
            @Param("confirmed") RequestStatus confirmed,
            Pageable pageable
    );
}
