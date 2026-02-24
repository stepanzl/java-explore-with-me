package ru.practicum.main.requests.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.main.requests.dto.ParticipationRequestDto;
import ru.practicum.main.requests.model.ParticipationRequest;
import ru.practicum.main.util.DateTimeMapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {DateTimeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ParticipationRequestMapper {

    @Mapping(target = "event", source = "event.id")
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "created", source = "created", qualifiedByName = "asString")
    ParticipationRequestDto toDto(ParticipationRequest request);

    List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> requests);
}