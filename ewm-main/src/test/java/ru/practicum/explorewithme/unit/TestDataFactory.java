package ru.practicum.explorewithme.unit;

import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.location.model.Location;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;

public class TestDataFactory {
    public static User createUser(String name, String email) {
        return new User(null, name, email);
    }

    public static Category createCategory(String name) {
        return new Category(null, name);
    }


    public static Event createEvent(User initiator, Category category, EventState eventState,
                                    boolean requestModeration, int participationLimit, int confirmedRequest) {
        return Event.builder()
                .title("Sample Event")
                .annotation("Quick summary")
                .description("This is a detailed description of the sample event.")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(0f, 0f))
                .paid(false)
                .participantLimit(participationLimit)
                .requestModeration(requestModeration)
                .confirmedRequests(confirmedRequest)
                .state(eventState)
                .initiator(initiator)
                .category(category)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
