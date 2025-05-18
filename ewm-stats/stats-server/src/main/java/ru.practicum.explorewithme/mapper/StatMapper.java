package ru.practicum.explorewithme.mapper;

import org.mapstruct.Mapper;
import ru.practicum.explorewithme.entity.Stat;
import ru.practicum.statsdto.StatDto;

@Mapper(componentModel = "spring")
public interface StatMapper {
    Stat toEntity(StatDto dto);

    StatDto toDto(Stat hit);
}
