package ru.practicum.explorewithme.service;

import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    StatDto createStat(StatDto dto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
