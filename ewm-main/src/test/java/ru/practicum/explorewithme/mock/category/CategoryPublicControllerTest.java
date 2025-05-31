package ru.practicum.explorewithme.mock.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.category.controller.common.CategoryPublicController;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.service.CategoryService;
import ru.practicum.explorewithme.exception.custom.NotFoundException;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryPublicController.class)
@AutoConfigureMockMvc
class CategoryPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnCategories() throws Exception {
        List<CategoryDto> categories = List.of(
                new CategoryDto(1L, "Music"),
                new CategoryDto(2L, "Sports")
        );

        Mockito.when(service.getCategories(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(categories.size()))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Music"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Sports"));
    }

    @Test
    void shouldReturnCategoryById() throws Exception {
        CategoryDto category = new CategoryDto(1L, "Education");

        Mockito.when(service.getCategoryById(1L)).thenReturn(category);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Education"));
    }

    @Test
    void shouldReturnNotFoundIfCategoryMissing() throws Exception {
        Mockito.when(service.getCategoryById(99L))
                .thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestOnInvalidId() throws Exception {
        mockMvc.perform(get("/categories/0"))
                .andExpect(status().isBadRequest());
    }
}
