package ru.practicum.main.locations.service;

import ru.practicum.main.locations.dto.LocationAreaDto;
import ru.practicum.main.locations.dto.NewLocationAreaDto;
import ru.practicum.main.locations.dto.UpdateLocationAreaRequest;
import ru.practicum.main.locations.model.LocationArea;
import ru.practicum.main.locations.model.LocationType;

import java.util.List;

public interface LocationAreaService {

    LocationAreaDto create(NewLocationAreaDto dto);

    List<LocationAreaDto> getAdminLocations(int from, int size);

    LocationAreaDto update(long locationId, UpdateLocationAreaRequest dto);

    void delete(long locationId);

    List<LocationAreaDto> getPublicLocations(LocationType type, String text, int from, int size);

    LocationArea getByIdOrThrow(long locationId);
}