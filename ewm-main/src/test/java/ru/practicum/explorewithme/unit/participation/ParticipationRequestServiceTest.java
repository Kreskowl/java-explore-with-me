package ru.practicum.explorewithme.unit.participation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.ForbiddenActionException;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.model.ParticipationRequest;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.participation.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.participation.service.ParticipationRequestService;
import ru.practicum.explorewithme.unit.AbstractServiceTest;
import ru.practicum.explorewithme.unit.TestDataFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ParticipationRequestServiceTest extends AbstractServiceTest {

    @Autowired
    private ParticipationRequestRepository repository;
    @Autowired
    private ParticipationRequestService service;
    @Autowired
    private EventRepository eventRepository;

    @Test
    void makeRequest_shouldReturnConfirmedStatus_whenModerationDisabled() {
        ParticipationRequestDto result = service.makeRequest(requester.getId(), event.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
        assertThat(result.getRequester()).isEqualTo(requester.getId());
        assertThat(result.getEvent()).isEqualTo(event.getId());

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getConfirmedRequests()).isEqualTo(1);
    }

    @Test
    void makeRequest_shouldReturnPending_whenModerationOn() {
        Event moderatedEvent = eventRepository
                .save(TestDataFactory.createEvent(initiator, category, EventState.PUBLISHED, true, 10, 0));

        ParticipationRequestDto result = service.makeRequest(requester.getId(), moderatedEvent.getId());

        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
        assertThat(eventRepository.findById(moderatedEvent.getId()).get().getConfirmedRequests()).isEqualTo(0);
    }

    @Test
    void makeRequest_shouldThrow_whenAlreadyExists() {
        service.makeRequest(requester.getId(), event.getId());

        assertThatThrownBy(() -> service.makeRequest(requester.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Duplicate request");
    }

    @Test
    void makeRequest_shouldThrow_whenLimitReached() {
        Event limitedEvent = eventRepository
                .save(TestDataFactory.createEvent(initiator, category, EventState.PUBLISHED,
                        false, 1, 1));

        assertThatThrownBy(() -> service.makeRequest(requester.getId(), limitedEvent.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void makeRequest_shouldThrow_whenRequesterIsInitiator() {
        assertThatThrownBy(() -> service.makeRequest(initiator.getId(), event.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Initiator cannot request");
    }

    @Test
    void makeRequest_shouldThrow_whenEventIsNotPublished() {
        Event notPublished = eventRepository
                .save(TestDataFactory.createEvent(initiator, category, EventState.PENDING,
                        false, 10, 0));

        assertThatThrownBy(() -> service.makeRequest(requester.getId(), notPublished.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not published");
    }

    @Test
    void cancelRequest_shouldSetStatusToPending_whenUserIsOwner() {
        ParticipationRequest saved = repository.save(
                new ParticipationRequest(null, LocalDateTime.now(), event, requester, RequestStatus.CONFIRMED)
        );

        ParticipationRequestDto result = service.cancelRequest(requester.getId(), saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getStatus()).isEqualTo(RequestStatus.CANCELED);

        ParticipationRequest updated = repository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.CANCELED);
    }

    @Test
    void cancelRequest_shouldThrowException_whenUserIsNotOwner() {
        ParticipationRequest saved = repository.save(
                new ParticipationRequest(null, LocalDateTime.now(), event, requester, RequestStatus.CONFIRMED)
        );

        assertThatThrownBy(() -> service.cancelRequest(initiator.getId(), saved.getId()))
                .isInstanceOf(ForbiddenActionException.class)
                .hasMessageContaining("does not belong to user with id");
    }

    @Test
    void getRequests_shouldReturnUserRequests() {
        ParticipationRequest request1 = repository.save(
                new ParticipationRequest(null, LocalDateTime.now(), event, requester, RequestStatus.PENDING)
        );
        ParticipationRequest request2 = repository.save(
                new ParticipationRequest(null, LocalDateTime.now(), event, requester, RequestStatus.CONFIRMED)
        );

        List<ParticipationRequestDto> result = service.getRequests(requester.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParticipationRequestDto::getId)
                .containsExactlyInAnyOrder(request1.getId(), request2.getId());
    }

    @Test
    void getRequests_shouldReturnEmptyList_whenNoRequests() {
        List<ParticipationRequestDto> result = service.getRequests(requester.getId());

        assertThat(result).isEmpty();
    }
}
