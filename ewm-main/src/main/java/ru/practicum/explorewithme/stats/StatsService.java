package ru.practicum.explorewithme.stats;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface StatsService {
    void saveHit(HttpServletRequest request);

    Map<String, Long> getViews(List<String> uris);
}
