package ru.practicum.main.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.PublicEventSort;
import ru.practicum.main.events.service.PublicEventService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping
public class PublicEventController {

    private final PublicEventService publicEventService;

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false) PublicEventSort sort,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "10") @Positive int size,
                                         HttpServletRequest request) {
        log.info("GET /events");
        return publicEventService.getPublicEvents(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request
        );
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEvent(@PathVariable @Positive long id, HttpServletRequest request) {
        log.info("GET /events/{}", id);
        return publicEventService.getPublicEvent(id, request);
    }
}