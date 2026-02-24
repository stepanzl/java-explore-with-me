package ru.practicum.main.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.practicum.main.events.dto.LocationDto;
import ru.practicum.main.events.model.Location;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LocationMapper {

    Location toEntity(LocationDto dto);

    LocationDto toDto(Location location);
}