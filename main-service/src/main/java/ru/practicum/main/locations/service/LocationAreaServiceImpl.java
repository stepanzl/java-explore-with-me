package ru.practicum.main.locations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.locations.dto.LocationAreaDto;
import ru.practicum.main.locations.dto.NewLocationAreaDto;
import ru.practicum.main.locations.dto.UpdateLocationAreaRequest;
import ru.practicum.main.locations.mapper.LocationAreaMapper;
import ru.practicum.main.locations.model.LocationArea;
import ru.practicum.main.locations.model.LocationType;
import ru.practicum.main.locations.repository.LocationAreaRepository;
import ru.practicum.main.util.PageRequestUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationAreaServiceImpl implements LocationAreaService {

    private final LocationAreaRepository repository;
    private final LocationAreaMapper mapper;

    @Override
    @Transactional
    public LocationAreaDto create(NewLocationAreaDto dto) {
        LocationArea entity = mapper.toEntity(dto);
        entity.setCreatedOn(LocalDateTime.now());
        LocationArea saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationAreaDto> getAdminLocations(int from, int size) {
        Pageable pageable = PageRequestUtil.from(from, size, null);
        return repository.findAll(pageable).getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public LocationAreaDto update(long locationId, UpdateLocationAreaRequest dto) {
        LocationArea entity = getByIdOrThrow(locationId);
        mapper.update(dto, entity);
        LocationArea saved = repository.save(entity);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(long locationId) {
        if (!repository.existsById(locationId)) {
            throw new NotFoundException(
                    "LocationArea with id=" + locationId + " was not found"
            );
        }
        repository.deleteById(locationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationAreaDto> getPublicLocations(LocationType type, String text, int from, int size) {
        Specification<LocationArea> spec = Specs.type(type).and(Specs.text(text));

        Pageable pageable = PageRequestUtil.from(from, size, null);

        return repository.findAll(spec, pageable).getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LocationArea getByIdOrThrow(long locationId) {
        return repository.findById(locationId)
                .orElseThrow(() -> new NotFoundException("LocationArea with id=" + locationId + " was not found"));
    }

    private static final class Specs {

        static Specification<LocationArea> type(LocationType type) {
            return (root, query, cb) -> {
                if (type == null) {
                    return cb.conjunction();
                }
                return cb.equal(root.get("type"), type);
            };
        }

        static Specification<LocationArea> text(String text) {
            return (root, query, cb) -> {
                if (text == null || text.isBlank()) {
                    return cb.conjunction();
                }
                String like = "%" + text.toLowerCase(Locale.ROOT) + "%";
                return cb.like(cb.lower(root.get("name")), like);
            };
        }
    }
}