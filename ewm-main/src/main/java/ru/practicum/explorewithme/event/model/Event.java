package ru.practicum.explorewithme.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.location.model.Location;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String title;
    @NotNull
    private String annotation;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private Integer confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    @NotNull
    private LocalDateTime eventDate;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiator;
    @NotNull
    @Embedded
    private Location location;
    @NotNull
    private Boolean paid;
    private Integer participantLimit;
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state = EventState.PENDING;
    private Long views;
}
