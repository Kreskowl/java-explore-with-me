package ru.practicum.explorewithme.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.participation.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
