package ru.practicum.main.events.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.PublicEventSort;
import ru.practicum.main.events.mapper.EventMapper;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventState;
import ru.practicum.main.events.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.requests.model.RequestStatus;
import ru.practicum.main.requests.repository.ParticipationRequestRepository;
import ru.practicum.main.stats.service.StatsService;
import ru.practicum.main.util.DateTimeMapper;
import ru.practicum.main.util.PageRequestUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final DateTimeMapper dateTimeMapper;
    private final StatsService statsService;

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               PublicEventSort sort,
                                               int from,
                                               int size,
                                               HttpServletRequest request) {

        statsService.hit(request);

        LocalDateTime start = parseOrNull(rangeStart);
        LocalDateTime end = parseOrNull(rangeEnd);
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }

        Specification<Event> spec = PublicSpecs.publishedOnly()
                .and(PublicSpecs.text(text))
                .and(PublicSpecs.categories(categories))
                .and(PublicSpecs.paid(paid))
                .and(PublicSpecs.range(start, end));

        List<Event> events;

        if (sort == PublicEventSort.VIEWS) {
            events = eventRepository.findAll(spec);
        } else {
            Sort s = Sort.by(Sort.Direction.ASC, "eventDate");
            PageRequest pageable = PageRequestUtil.from(from, size, s);
            events = eventRepository.findAll(spec, pageable).getContent();
        }

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> confirmedMap = getConfirmedMap(events);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(e -> isAvailable(e, confirmedMap.getOrDefault(e.getId(), 0L)))
                    .toList();
        }

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> viewsMap = statsService.getEventViewsMap(
                events.stream().map(Event::getId).collect(Collectors.toSet()),
                true
        );

        List<EventShortDto> dtos = new ArrayList<>(events.stream()
                .map(eventMapper::toShortDto)
                .toList());

        enrich(dtos, confirmedMap, viewsMap);

        if (sort == PublicEventSort.VIEWS) {
            dtos.sort(Comparator.comparingLong(
                    (EventShortDto d) -> d.getViews() == null ? 0L : d.getViews()
            ).reversed());
            return slice(dtos, from, size);
        }

        return dtos;
    }

    @Override
    public EventFullDto getPublicEvent(long eventId, HttpServletRequest request) {
        statsService.hit(request);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        EventFullDto dto = eventMapper.toFullDto(event);

        Map<Long, Long> confirmedMap = Map.of(
                eventId,
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
        );

        Map<Long, Long> viewsMap = statsService.getEventViewsMap(
                List.of(eventId),
                true
        );

        enrich(List.of(dto), confirmedMap, viewsMap);

        return dto;
    }

    private LocalDateTime parseOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        return dateTimeMapper.asLocalDateTime(value);
    }

    private Map<Long, Long> getConfirmedMap(List<Event> events) {
        Map<Long, Long> map = new HashMap<>();
        for (Event e : events) {
            long c = requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED);
            map.put(e.getId(), c);
        }
        return map;
    }

    private boolean isAvailable(Event e, long confirmed) {
        Integer limit = e.getParticipantLimit();
        if (limit == null || limit == 0) return true;
        return confirmed < limit;
    }

    private <T> void enrich(List<T> dtos,
                            Map<Long, Long> confirmedMap,
                            Map<Long, Long> viewsMap) {

        for (T dto : dtos) {

            Long id;

            if (dto instanceof EventShortDto shortDto) {
                id = shortDto.getId();
                shortDto.setConfirmedRequests((int) confirmedMap.getOrDefault(id, 0L).longValue());
                shortDto.setViews(viewsMap.getOrDefault(id, 0L));
            }

            if (dto instanceof EventFullDto fullDto) {
                id = fullDto.getId();
                fullDto.setConfirmedRequests((int) confirmedMap.getOrDefault(id, 0L).longValue());
                fullDto.setViews(viewsMap.getOrDefault(id, 0L));
            }
        }
    }

    private <T> List<T> slice(List<T> list, int from, int size) {
        if (from >= list.size()) return List.of();
        int to = Math.min(list.size(), from + size);
        return list.subList(from, to);
    }

    private static final class PublicSpecs {

        static Specification<Event> publishedOnly() {
            return (root, query, cb) ->
                    cb.equal(root.get("state"), EventState.PUBLISHED);
        }

        static Specification<Event> text(String text) {
            if (text == null || text.isBlank()) return null;
            String like = "%" + text.toLowerCase(Locale.ROOT) + "%";
            return (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("annotation")), like),
                    cb.like(cb.lower(root.get("description")), like)
            );
        }

        static Specification<Event> categories(List<Long> categories) {
            if (categories == null || categories.isEmpty()) return null;
            return (root, query, cb) -> root.get("category").get("id").in(categories);
        }

        static Specification<Event> paid(Boolean paid) {
            if (paid == null) return null;
            return (root, query, cb) -> cb.equal(root.get("paid"), paid);
        }

        static Specification<Event> range(LocalDateTime start, LocalDateTime end) {
            if (start == null && end == null) {
                return (root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now());
            }
            if (start != null && end != null) {
                return (root, query, cb) ->
                        cb.between(root.get("eventDate"), start, end);
            }
            if (start != null) {
                return (root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            }
            return (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), end);
        }
    }
}