package ru.practicum.main.stats.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.util.DateTimeFormat;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String EVENT_URI_PREFIX = "/events/";
    private static final Pattern EVENT_URI_PATTERN = Pattern.compile("^/events/(\\d+)$");

    @Value("${spring.application.name:ewm-main-service}")
    private String appName;

    private final StatsClient statsClient;

    public void hit(HttpServletRequest request) {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(extractClientIp(request))
                .timestamp(LocalDateTime.now().format(DateTimeFormat.FORMATTER))
                .build();

        statsClient.addStats(dto);
    }

    public Map<Long, Long> getViews(Iterable<Event> events) {
        if (events == null) {
            return Collections.emptyMap();
        }

        List<Long> ids = new ArrayList<>();
        for (Event e : events) {
            if (e != null && e.getId() != null) {
                ids.add(e.getId());
            }
        }

        return getEventViewsMap(ids, true);
    }

    public Map<Long, Long> getEventViewsMap(Collection<Long> eventIds, boolean unique) {
        Map<Long, Long> views = new HashMap<>();
        if (eventIds == null || eventIds.isEmpty()) {
            return views;
        }

        List<String> uris = new ArrayList<>();
        for (Long id : eventIds) {
            if (id == null) continue;
            views.put(id, 0L);
            uris.add(EVENT_URI_PREFIX + id);
        }

        if (uris.isEmpty()) {
            return views;
        }

        String start = LocalDateTime.of(2000, 1, 1, 0, 0, 0).format(DateTimeFormat.FORMATTER);
        String end = LocalDateTime.now().format(DateTimeFormat.FORMATTER);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, unique);

        for (ViewStatsDto s : stats) {
            String uri = s.getUri();
            if (uri == null) continue;

            Matcher m = EVENT_URI_PATTERN.matcher(uri);
            if (m.matches()) {
                long eventId = Long.parseLong(m.group(1));
                views.put(eventId, s.getHits() == null ? 0L : s.getHits());
            }
        }

        return views;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}