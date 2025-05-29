package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.entity.Stat;
import ru.practicum.explorewithme.exception.InvalidUriParameterException;
import ru.practicum.explorewithme.exception.TimeRangeValidationException;
import ru.practicum.explorewithme.mapper.StatMapper;
import ru.practicum.statsdto.Constants;
import ru.practicum.statsdto.StatDto;
import ru.practicum.statsdto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;
    private final StatMapper mapper;

    @Override
    @Transactional
    public StatDto createStat(StatDto dto) {
        Stat stat = repository.save(mapper.toEntity(dto));
        return mapper.toDto(stat);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {
        validateRequestParams(start, end);
        validateUrisParams(uris);

        return unique
                ? repository.getStatsUnique(start, end, uris)
                : repository.getStatsNonUnique(start, end, uris);
    }

    private void validateRequestParams(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new TimeRangeValidationException("Start must not be after end");
        }
    }

    private void validateUrisParams(List<String> uris) {
        if (uris == null) return;

        if (uris.isEmpty() || uris.stream().anyMatch(u -> u == null || u.isBlank())) {
            throw new InvalidUriParameterException("'uris' must not be empty or contain blank/null values");
        }

        for (String uri : uris) {
            if (!uri.matches(Constants.URI_VALID_FORMAT)) {
                throw new InvalidUriParameterException("Invalid URI format: " + uri);
            }
        }
    }
}
