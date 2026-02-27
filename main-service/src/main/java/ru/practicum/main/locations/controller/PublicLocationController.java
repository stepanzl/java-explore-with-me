package ru.practicum.main.locations.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.locations.dto.LocationAreaDto;
import ru.practicum.main.locations.model.LocationType;
import ru.practicum.main.locations.service.LocationAreaService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class PublicLocationController {

    private final LocationAreaService service;

    @GetMapping
    public List<LocationAreaDto> getLocations(@RequestParam(required = false) LocationType type,
                                              @RequestParam(required = false) String text,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /locations");
        return service.getPublicLocations(type, text, from, size);
    }
}