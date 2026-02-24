package ru.practicum.main.compilations.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.compilations.model.Compilation;
import ru.practicum.main.events.mapper.EventMapper;

@Mapper(
        componentModel = "spring",
        uses = {EventMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CompilationMapper {

    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Compilation toEntity(NewCompilationDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void update(UpdateCompilationRequest dto, @MappingTarget Compilation compilation);
}