package ru.practicum.explorewithme.participation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.participation.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.participation.model.ParticipationRequest;
import ru.practicum.explorewithme.participation.model.RequestStatus;
import ru.practicum.explorewithme.user.model.User;

@Mapper(componentModel = "spring")
public interface ParticipationRequestMapper {
    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    ParticipationRequestDto toDto(ParticipationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "event", target = "event")
    @Mapping(source = "requester", target = "requester")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", source = "status")
    ParticipationRequest toEntity(Event event, User requester, RequestStatus status);
}
