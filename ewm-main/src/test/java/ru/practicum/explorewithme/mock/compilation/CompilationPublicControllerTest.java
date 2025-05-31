package ru.practicum.explorewithme.mock.compilation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.service.CompilationService;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CompilationPublicControllerTest {
    @MockBean
    private CompilationService service;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCompilations_shouldReturnFilteredList() throws Exception {
        CompilationDto dto1 = new CompilationDto(1L, true, "Comp 1", Set.of());
        CompilationDto dto2 = new CompilationDto(2L, true, "Comp 2", Set.of());

        Mockito.when(service.getCompilations(true, 0, 10))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Comp 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Comp 2"));
    }

    @Test
    void getCompilationById_shouldReturnOne() throws Exception {
        CompilationDto dto = new CompilationDto(5L, false, "Single Comp", Set.of());

        Mockito.when(service.getCompilationById(5L)).thenReturn(dto);

        mockMvc.perform(get("/compilations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Single Comp"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void getCompilations_shouldReturnEmptyList() throws Exception {
        Mockito.when(service.getCompilations(null, 0, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
