package ru.practicum.main.compilations.service;

import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto dto);

    CompilationDto update(long compId, UpdateCompilationRequest dto);

    void delete(long compId);

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(long compId);
}