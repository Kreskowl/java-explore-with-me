package ru.practicum.explorewithme.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.event.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.location.mapper.LocationMapper;
import ru.practicum.explorewithme.user.mapper.UserMapper;
import ru.practicum.explorewithme.user.model.User;

@Mapper(
        componentModel = "spring",
        uses = {
                CategoryMapper.class,
                LocationMapper.class,
                UserMapper.class
        },
        nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    EventFullDto toFullDto(Event event);

    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiator", target = "initiator")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "confirmedRequests", constant = "0")
    @Mapping(target = "views", constant = "0L")
    Event toEntity(NewEventDto dto, Category category, User initiator);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEventFromUserRequest(UpdateEventUserRequest request, @MappingTarget Event event);

    @Mapping(target = "state", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEventFromAdminRequest(UpdateEventAdminRequest request, @MappingTarget Event event);
}
