package ru.practicum.main.events.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventUserRequest;
import ru.practicum.main.events.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable @Positive Long userId,
                               @Valid @RequestBody NewEventDto dto) {
        log.info("POST /users/{}/events", userId);
        return eventService.createPrivate(userId, dto);
    }

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable @Positive Long userId,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                      @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /users/{}/events", userId);
        return eventService.getPrivateEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId) {
        log.info("GET /users/{}/events/{}", userId, eventId);
        return eventService.getPrivateEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable @Positive Long userId,
                               @PathVariable @Positive Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest dto) {
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return eventService.updatePrivate(userId, eventId, dto);
    }
}