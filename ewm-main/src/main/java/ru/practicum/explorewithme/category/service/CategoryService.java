package ru.practicum.explorewithme.category.service;

import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getCategories(long from, long size);

    CategoryDto getCategoryById(long catId);

    CategoryDto addCategory(NewCategoryDto dto);

    void deleteCategory(long catId);

    CategoryDto updateCategory(long catId, CategoryDto dto);
}
