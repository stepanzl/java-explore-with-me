package ru.practicum.stats.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.ValidationException;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;

    public StatsServiceImpl(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Override
    @Transactional
    public void saveHit(EndpointHitDto dto) {
        hitRepository.save(HitMapper.toEntity(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("start must be before or equal to end");
        }

        boolean hasUris = uris != null && !uris.isEmpty();

        if (unique) {
            return hasUris
                    ? hitRepository.findStatsUniqueByUris(start, end, uris)
                    : hitRepository.findStatsUnique(start, end);
        }

        return hasUris
                ? hitRepository.findStatsByUris(start, end, uris)
                : hitRepository.findStats(start, end);
    }
}
