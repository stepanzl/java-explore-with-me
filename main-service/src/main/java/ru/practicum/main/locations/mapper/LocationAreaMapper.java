package ru.practicum.main.locations.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.main.locations.dto.LocationAreaDto;
import ru.practicum.main.locations.dto.NewLocationAreaDto;
import ru.practicum.main.locations.dto.UpdateLocationAreaRequest;
import ru.practicum.main.locations.model.LocationArea;
import ru.practicum.main.util.DateTimeMapper;

@Mapper(
        componentModel = "spring",
        uses = {DateTimeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LocationAreaMapper {

    @Mapping(target = "type", expression = "java(entity.getType().name())")
    @Mapping(target = "createdOn", source = "createdOn", qualifiedByName = "asString")
    LocationAreaDto toDto(LocationArea entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    LocationArea toEntity(NewLocationAreaDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    void update(UpdateLocationAreaRequest dto, @MappingTarget LocationArea entity);
}