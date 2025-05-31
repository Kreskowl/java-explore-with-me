package ru.practicum.explorewithme.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.Constants;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsClient statsClient;

    @Override
    public void saveHit(HttpServletRequest request) {
        statsClient.saveHit(new StatDto(
                Constants.APP_NAME,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        ));
    }

    @Override
    public Map<String, Long> getViews(List<String> uris) {
        return statsClient.getViews(uris);
    }
}
