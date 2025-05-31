package ru.practicum.explorewithme.location.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.location.dto.LocationDto;
import ru.practicum.explorewithme.location.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toEntity(LocationDto dto);
}
