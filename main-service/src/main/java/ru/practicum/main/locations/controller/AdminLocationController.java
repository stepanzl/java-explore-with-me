package ru.practicum.main.locations.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.locations.dto.LocationAreaDto;
import ru.practicum.main.locations.dto.NewLocationAreaDto;
import ru.practicum.main.locations.dto.UpdateLocationAreaRequest;
import ru.practicum.main.locations.service.LocationAreaService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/locations")
public class AdminLocationController {

    private final LocationAreaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationAreaDto create(@RequestBody @Valid NewLocationAreaDto dto) {
        log.info("POST /admin/locations");
        return service.create(dto);
    }

    @GetMapping
    public List<LocationAreaDto> getAll(@RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /admin/locations");
        return service.getAdminLocations(from, size);
    }

    @PatchMapping("/{locationId}")
    public LocationAreaDto update(@PathVariable @Positive long locationId,
                                  @RequestBody @Valid UpdateLocationAreaRequest dto) {
        log.info("PATCH /admin/locations/{}", locationId);
        return service.update(locationId, dto);
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive long locationId) {
        log.info("DELETE /admin/locations/{}", locationId);
        service.delete(locationId);
    }
}