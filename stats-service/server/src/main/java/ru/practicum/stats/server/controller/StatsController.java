package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;
import ru.practicum.stats.server.util.StatsDateTime;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHitDto dto) {
        statsService.saveHit(dto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> stats(
            @RequestParam
            @DateTimeFormat(pattern = StatsDateTime.PATTERN)
            LocalDateTime start,

            @RequestParam
            @DateTimeFormat(pattern = StatsDateTime.PATTERN)
            LocalDateTime end,

            @RequestParam(required = false)
            List<String> uris,

            @RequestParam(defaultValue = "false")
            boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }
}
