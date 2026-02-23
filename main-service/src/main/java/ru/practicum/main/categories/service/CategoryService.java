package ru.practicum.main.categories.service;

import ru.practicum.main.categories.dto.CategoryDto;
import ru.practicum.main.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(long catId, NewCategoryDto dto);

    void delete(long catId);

    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(long catId);
}