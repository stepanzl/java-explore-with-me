package ru.practicum.main.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.compilations.mapper.CompilationMapper;
import ru.practicum.main.compilations.model.Compilation;
import ru.practicum.main.compilations.repository.CompilationRepository;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.util.PageRequestUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        log.info("Create compilation: title={}, pinned={}, eventsProvided={}",
                dto.getTitle(), dto.getPinned(), dto.getEvents() != null);

        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Compilation with title='" + dto.getTitle() + "' already exists");
        }

        Compilation compilation = compilationMapper.toEntity(dto);
        compilation.setPinned(Boolean.TRUE.equals(dto.getPinned()));
        compilation.setEvents(resolveEvents(dto.getEvents()));

        Compilation saved = compilationRepository.save(compilation);
        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto update(long compId, UpdateCompilationRequest dto) {
        log.info("Update compilation: compId={}, titlePresent={}, pinnedPresent={}, eventsPresent={}",
                compId, dto.getTitle() != null, dto.getPinned() != null, dto.getEvents() != null);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (dto.getTitle() != null && !dto.getTitle().equals(compilation.getTitle())
                && compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Compilation with title='" + dto.getTitle() + "' already exists");
        }

        compilationMapper.update(dto, compilation);

        if (dto.getEvents() != null) {
            compilation.setEvents(resolveEvents(dto.getEvents()));
        }

        Compilation saved = compilationRepository.save(compilation);
        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(long compId) {
        log.info("Delete compilation: compId={}", compId);

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        log.info("Get compilations: pinned={}, from={}, size={}", pinned, from, size);

        Pageable pageable = PageRequestUtil.from(from, size);

        if (pinned == null) {
            return compilationRepository.findAll(pageable)
                    .stream()
                    .map(compilationMapper::toDto)
                    .toList();
        }

        return compilationRepository.findAllByPinned(pinned, pageable)
                .stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getById(long compId) {
        log.info("Get compilation by id: compId={}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        return compilationMapper.toDto(compilation);
    }

    private Set<Event> resolveEvents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        List<Event> found = eventRepository.findAllById(ids);

        if (found.size() != new HashSet<>(ids).size()) {
            throw new NotFoundException("One or more events were not found");
        }

        return new HashSet<>(found);
    }
}