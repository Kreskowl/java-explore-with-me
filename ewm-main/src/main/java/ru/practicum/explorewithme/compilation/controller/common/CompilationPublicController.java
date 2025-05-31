package ru.practicum.explorewithme.compilation.controller.common;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.Constants;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.service.CompilationService;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/compilations")
public class CompilationPublicController {
    private final CompilationService service;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = Constants.DEFAULT_FROM_VALUE)
                                                @PositiveOrZero int from,
                                                @RequestParam(defaultValue = Constants.DEFAULT_SIZE_VALUE)
                                                @Positive int size) {
        return service.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable long compId) {
        return service.getCompilationById(compId);
    }
}
