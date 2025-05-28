package ru.practicum.explorewithme.unit.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.dto.NewCategoryDto;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.category.service.CategoryService;
import ru.practicum.explorewithme.event.model.EventState;
import ru.practicum.explorewithme.exception.custom.ConflictException;
import ru.practicum.explorewithme.exception.custom.NotFoundException;
import ru.practicum.explorewithme.unit.AbstractServiceTest;
import ru.practicum.explorewithme.unit.TestDataFactory;
import ru.practicum.explorewithme.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CategoryServiceTest extends AbstractServiceTest {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void addCategory_shouldPersistAndReturnDto() {
        NewCategoryDto dto = new NewCategoryDto("Concerts");

        CategoryDto saved = categoryService.addCategory(dto);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Concerts");
        assertThat(categoryRepository.existsByName("Concerts")).isTrue();
    }

    @Test
    void getCategoryById_shouldReturnDto() {
        Category saved = categoryRepository.save(TestDataFactory.createCategory("Theatre"));

        CategoryDto result = categoryService.getCategoryById(saved.getId());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Theatre");
    }

    @Test
    void getCategories_shouldReturnList() {
        categoryRepository.save(TestDataFactory.createCategory("One"));
        categoryRepository.save(TestDataFactory.createCategory("Two"));

        List<CategoryDto> result = categoryService.getCategories(0, 10);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void updateCategory_shouldModifyName() {
        Category saved = categoryRepository.save(TestDataFactory.createCategory("OldName"));
        CategoryDto dto = new CategoryDto(saved.getId(), "NewName");

        CategoryDto updated = categoryService.updateCategory(saved.getId(), dto);

        assertThat(updated.getName()).isEqualTo("NewName");
    }

    @Test
    void deleteCategory_shouldRemoveEntity() {
        Category saved = categoryRepository.save(TestDataFactory.createCategory("DeleteMe"));

        categoryService.deleteCategory(saved.getId());

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void getCategoryById_shouldThrowNotFound() {
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addCategory_shouldThrowConflictIfNameExists() {
        categoryRepository.save(TestDataFactory.createCategory("Duplicate"));

        NewCategoryDto dto = new NewCategoryDto("Duplicate");

        assertThatThrownBy(() -> categoryService.addCategory(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateCategory_shouldThrowConflictIfNameTaken() {
        categoryRepository.save(TestDataFactory.createCategory("Existing"));
        Category toUpdate = categoryRepository.save(TestDataFactory.createCategory("ToChange"));

        CategoryDto dto = new CategoryDto(toUpdate.getId(), "Existing");

        assertThatThrownBy(() -> categoryService.updateCategory(toUpdate.getId(), dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteCategory_shouldThrowConflictIfLinkedEventsExist() {
        Category tech = categoryRepository.save(TestDataFactory.createCategory("Tech"));
        User user = userRepository.save(TestDataFactory.createUser("test", "email@test.com"));

        eventRepository.save(TestDataFactory.createEvent(user, tech, EventState.PUBLISHED,
                false, 10, 0));

        assertThatThrownBy(() -> categoryService.deleteCategory(tech.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("associated events exist");
    }
}

