package ru.practicum.explorewithme.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryDto> getCategories(long from, long size) {
        Pageable pageable = PageRequest.of((int) (from / size), (int) size);
        return repository.findAll(pageable).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Category category = ifCategoryExists(catId);
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (repository.existsByName(dto.getName())) {
            throw new ConflictException("Category name must be unique: " + dto.getName());
        }

        Category category = repository.save(mapper.toEntity(dto));
        return mapper.toDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        Category category = ifCategoryExists(catId);
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Cannot delete category: associated events exist");
        }
        repository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, CategoryDto dto) {
        Category category = ifCategoryExists(catId);
        if (repository.existsByName(dto.getName()) && !category.getName().equals(dto.getName())) {
            throw new ConflictException("Category name must be unique: " + dto.getName());
        }
        category.setName(dto.getName());

        Category updated = repository.save(category);
        return mapper.toDto(updated);
    }

    private Category ifCategoryExists(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("category with id " + id + " not found"));
    }
}
