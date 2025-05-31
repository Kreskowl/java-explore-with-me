package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class NewCompilationDto {
    private Boolean pinned;
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    private Set<Long> events = new HashSet<>();
}
