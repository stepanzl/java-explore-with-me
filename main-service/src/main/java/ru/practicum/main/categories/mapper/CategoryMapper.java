package ru.practicum.main.categories.mapper;

import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.NewCategoryDto;
import ru.practicum.main.categories.model.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toEntity(NewCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static void update(Category category, NewCategoryDto dto) {
        if (category == null || dto == null) {
            return;
        }
        category.setName(dto.getName());
    }
}