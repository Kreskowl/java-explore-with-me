package ru.practicum.explorewithme.mock.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.category.controller.admin.CategoryAdminController;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.category.service.CategoryService;
import ru.practicum.explorewithme.exception.custom.ConflictException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryAdminController.class)
@AutoConfigureMockMvc
class CategoryAdminControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoryService service;

    @Test
    void shouldAddCategory() throws Exception {
        NewCategoryDto newDto = new NewCategoryDto("Rock");
        CategoryDto saved = new CategoryDto(1L, "Rock");

        Mockito.when(service.addCategory(Mockito.any())).thenReturn(saved);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value(saved.getName()));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/admin/categories/{catId}", 1))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteCategory(1L);
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryDto input = new CategoryDto(1L, "Updated");
        CategoryDto updated = new CategoryDto(1L, "Updated");

        Mockito.when(service.updateCategory(Mockito.eq(1L), Mockito.any()))
                .thenReturn(updated);

        mockMvc.perform(patch("/admin/categories/{catId}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updated.getId()))
                .andExpect(jsonPath("$.name").value(updated.getName()));
    }

    @Test
    void shouldReturn409IfCategoryExists() throws Exception {
        NewCategoryDto newDto = new NewCategoryDto("Duplicate");

        Mockito.when(service.addCategory(Mockito.any()))
                .thenThrow(new ConflictException("Category already exists"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isConflict());
    }
}
