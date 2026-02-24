package ru.practicum.main.compilations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.compilations.service.CompilationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDto create(@RequestBody @Valid NewCompilationDto dto) {
        log.info("POST /admin/compilations");
        return compilationService.create(dto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable long compId,
                                 @RequestBody @Valid UpdateCompilationRequest dto) {
        log.info("PATCH /admin/compilations/{}", compId);
        return compilationService.update(compId, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void delete(@PathVariable long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        compilationService.delete(compId);
    }
}