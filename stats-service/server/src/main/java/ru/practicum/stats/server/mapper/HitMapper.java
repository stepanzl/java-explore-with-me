package ru.practicum.stats.server.mapper;

import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.exception.ValidationException;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.util.StatsDateTime;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class HitMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(StatsDateTime.PATTERN);

    private HitMapper() {
    }

    public static Hit toEntity(EndpointHitDto dto) {
        if (dto == null) {
            return null;
        }

        return Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(parseTimestamp(dto.getTimestamp()))
                .build();
    }

    private static LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            throw new ValidationException("timestamp must not be blank");
        }
        try {
            return LocalDateTime.parse(timestamp, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("timestamp must match pattern " + StatsDateTime.PATTERN + ": " + timestamp);
        }
    }
}