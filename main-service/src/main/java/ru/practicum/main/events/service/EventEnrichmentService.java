package ru.practicum.main.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.requests.model.RequestStatus;
import ru.practicum.main.requests.repository.ParticipationRequestRepository;
import ru.practicum.main.stats.service.StatsService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventEnrichmentService {

    private final StatsService statsService;
    private final ParticipationRequestRepository requestRepository;

    public void enrichShort(List<Event> events, List<EventShortDto> dtos) {
        Map<Long, EventShortDto> dtoById = dtos.stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(EventShortDto::getId, Function.identity(), (a, b) -> a));

        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Integer> confirmed = getConfirmedCounts(events);

        for (Event e : events) {
            Long id = e.getId();
            EventShortDto dto = dtoById.get(id);
            if (dto == null) continue;

            dto.setViews(views.getOrDefault(id, 0L));
            dto.setConfirmedRequests(confirmed.getOrDefault(id, 0));
        }
    }

    public void enrichFull(List<Event> events, List<EventFullDto> dtos) {
        Map<Long, EventFullDto> dtoById = dtos.stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(EventFullDto::getId, Function.identity(), (a, b) -> a));

        Map<Long, Long> views = statsService.getViews(events);
        Map<Long, Integer> confirmed = getConfirmedCounts(events);

        for (Event e : events) {
            Long id = e.getId();
            EventFullDto dto = dtoById.get(id);
            if (dto == null) continue;

            dto.setViews(views.getOrDefault(id, 0L));
            dto.setConfirmedRequests(confirmed.getOrDefault(id, 0));
        }
    }

    public void enrichShortOne(Event event, EventShortDto dto) {
        if (event.getId() == null || dto.getId() == null) return;
        Long id = event.getId();
        dto.setViews(statsService.getViews(List.of(event)).getOrDefault(id, 0L));
        dto.setConfirmedRequests(getConfirmedCounts(List.of(event)).getOrDefault(id, 0));
    }

    public void enrichFullOne(Event event, EventFullDto dto) {
        if (event.getId() == null || dto.getId() == null) return;
        Long id = event.getId();
        dto.setViews(statsService.getViews(List.of(event)).getOrDefault(id, 0L));
        dto.setConfirmedRequests(getConfirmedCounts(List.of(event)).getOrDefault(id, 0));
    }

    private Map<Long, Integer> getConfirmedCounts(List<Event> events) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Event e : events) {
            Long id = e.getId();
            if (id == null) continue;
            long c = requestRepository.countByEventIdAndStatus(id, RequestStatus.CONFIRMED);
            counts.put(id, (int) c);
        }
        return counts;
    }
}