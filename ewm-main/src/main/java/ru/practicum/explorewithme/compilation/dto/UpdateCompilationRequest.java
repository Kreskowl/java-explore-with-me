package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class UpdateCompilationRequest {
    private boolean pinned;
    @Size(min = 1, max = 50)
    private String title;
    private Set<Long> events;
}
