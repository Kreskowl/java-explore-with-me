package ru.practicum.explorewithme.comment.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.explorewithme.Constants;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentSearchParams {
    private List<Long> userIds;
    private List<Long> eventIds;
    private List<Long> commentIds;
    private String text;
    @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = Constants.DATE_TIME_PATTERN)
    private LocalDateTime rangeEnd;
    private Sort.Direction sort = Sort.Direction.DESC;
    @Min(0)
    private Integer from = 0;
    @Min(1)
    private Integer size = 10;
}
