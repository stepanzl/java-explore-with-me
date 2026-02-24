package ru.practicum.main.events.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.practicum.main.categories.mapper.CategoryMapper;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventAdminRequest;
import ru.practicum.main.events.dto.UpdateEventUserRequest;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.users.mapper.UserMapper;
import ru.practicum.main.util.DateTimeMapper;

@Mapper(
        componentModel = "spring",
        uses = {CategoryMapper.class, UserMapper.class, LocationMapper.class, DateTimeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {

    @Mapping(target = "createdOn", source = "createdOn", qualifiedByName = "asString")
    @Mapping(target = "eventDate", source = "eventDate", qualifiedByName = "asString")
    @Mapping(target = "publishedOn", source = "publishedOn", qualifiedByName = "asString")
    @Mapping(target = "views", expression = "java(0L)")
    @Mapping(target = "confirmedRequests", expression = "java(0)")
    EventFullDto toFullDto(Event event);

    @Mapping(target = "eventDate", source = "eventDate", qualifiedByName = "asString")
    @Mapping(target = "views", expression = "java(0L)")
    @Mapping(target = "confirmedRequests", expression = "java(0)")
    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate", qualifiedByName = "asLocalDateTime")
    Event toEntity(NewEventDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate", qualifiedByName = "asLocalDateTime")
    void updateFromUser(UpdateEventUserRequest dto, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate", qualifiedByName = "asLocalDateTime")
    void updateFromAdmin(UpdateEventAdminRequest dto, @MappingTarget Event event);
}