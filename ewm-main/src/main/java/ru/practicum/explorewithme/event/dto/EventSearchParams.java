package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explorewithme.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class EventSearchParams {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable = false;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private SortType sort;
    @Min(0)
    private int from = Integer.parseInt(Constants.DEFAULT_FROM_VALUE);
    @Min(1)
    private int size = Integer.parseInt(Constants.DEFAULT_SIZE_VALUE);
}
