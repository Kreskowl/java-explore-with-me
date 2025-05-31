package ru.practicum.explorewithme.unit.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.compilation.repository.CompilationRepository;
import ru.practicum.explorewithme.compilation.service.CompilationService;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.unit.AbstractServiceTest;
import ru.practicum.explorewithme.unit.TestDataFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Transactional
public class CompilationServiceTest extends AbstractServiceTest {
    @Autowired
    private CompilationService service;

    @Autowired
    private CompilationRepository repository;

    @Autowired
    private CompilationMapper mapper;

    @Test
    void getCompilationById_shouldReturnDto_whenExists() {
        Compilation saved = repository.save(new Compilation(null, false, "Compilation 1", Set.of()));
        CompilationDto result = service.getCompilationById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo(saved.getTitle());
    }

    @Test
    void getCompilationById_shouldThrow_whenNotExists() {
        assertThatThrownBy(() -> service.getCompilationById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Compilation with id 999 not found");
    }

    @Test
    void getCompilations_shouldReturnFilteredList_whenPinnedIsTrue() {
        repository.save(new Compilation(null, true, "Pinned 1", Set.of()));
        repository.save(new Compilation(null, true, "Pinned 2", Set.of()));

        List<CompilationDto> result = service.getCompilations(true, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CompilationDto::getTitle)
                .containsExactlyInAnyOrder("Pinned 1", "Pinned 2");
        assertThat(result).allMatch(CompilationDto::getPinned);
    }

    @Test
    void getCompilations_shouldReturnAll_whenPinnedIsNull() {
        repository.save(new Compilation(null, true, "Pinned", Set.of()));
        repository.save(new Compilation(null, false, "Unpinned", Set.of()));

        List<CompilationDto> result = service.getCompilations(null, 0, 10);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CompilationDto::getTitle)
                .containsExactlyInAnyOrder("Pinned", "Unpinned");
    }

    @Test
    void createCompilation_shouldPersistWithEventsAndPinnedFalse() {
        Event event = eventRepository.save(TestDataFactory.createEvent(initiator, category,
                EventState.PUBLISHED, false, 10, 0));

        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("New Comp");
        dto.setEvents(Set.of(event.getId()));

        CompilationDto created = service.createCompilation(dto);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("New Comp");
        assertThat(created.getPinned()).isFalse();
        assertThat(created.getEvents()).hasSize(1);
        assertThat(created.getEvents().stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toSet()))
                .contains(event.getId());
    }

    @Test
    void deleteCompilation_shouldRemoveFromRepo() {
        Compilation saved = repository.save(new Compilation(null, false, "To delete", Set.of()));

        service.deleteCompilation(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void getCompilations_shouldReturnEmpty_whenSizeIsZero() {
        repository.save(new Compilation(null, true, "Pinned", Set.of()));

        List<CompilationDto> result = service.getCompilations(true, 0, 0);

        assertThat(result).isEmpty();
    }

    @Test
    void getCompilations_shouldReturnEmpty_whenNoMatchesFound() {
        repository.save(new Compilation(null, false, "Only Unpinned", Set.of()));

        List<CompilationDto> result = service.getCompilations(true, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void updateCompilation_shouldUpdateTitleAndEvents() {
        Event initialEvent = eventRepository.save(TestDataFactory.createEvent(initiator, category,
                EventState.PUBLISHED, false, 10, 0));
        Compilation saved = repository.save(new Compilation(null, false,
                "Original Title", Set.of(initialEvent)));

        UpdateCompilationRequest update = new UpdateCompilationRequest();
        update.setTitle("Updated Title");
        update.setPinned(true);
        update.setEvents(Set.of(initialEvent.getId()));

        CompilationDto result = service.updateCompilation(saved.getId(), update);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getPinned()).isTrue();
        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getEvents().stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toSet()))
                .containsExactly(initialEvent.getId());
    }

    @Test
    void updateCompilation_shouldThrow_whenCompilationNotFound() {
        UpdateCompilationRequest update = new UpdateCompilationRequest();
        update.setTitle("Should Fail");

        assertThatThrownBy(() -> service.updateCompilation(999L, update))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Compilation with id=999 was not found");
    }

    @Test
    void shouldMapEntityToDto() {
        Compilation entity = new Compilation(1L, true, "Title", Set.of());
        CompilationDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Title");
    }
}
