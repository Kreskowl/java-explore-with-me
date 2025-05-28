package ru.practicum.explorewithme.unit;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.location.model.Location;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractServiceTest {
    protected final LocalDateTime created = LocalDateTime.now();
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected EventRepository eventRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    protected User requester;
    protected User initiator;
    protected Category category;
    protected Event event;

    @BeforeEach
    public void commonSetUp() {
        requester = userRepository.save(new User(null, "Requester", "req@test.com"));
        initiator = userRepository.save(new User(null, "Initiator", "init@test.com"));
        category = categoryRepository.save(new Category(null, "Category"));

        event = eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Quick summary")
                .description("This is a detailed description of the sample event.")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(new Location(50f, 50f))
                .paid(false)
                .participantLimit(10)
                .requestModeration(false)
                .confirmedRequests(0)
                .state(EventState.PUBLISHED)
                .initiator(initiator)
                .category(category)
                .createdOn(LocalDateTime.now())
                .build());
    }
}
