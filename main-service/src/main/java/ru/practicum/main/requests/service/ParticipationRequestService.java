package ru.practicum.main.requests.service;

import ru.practicum.main.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto create(long userId, long eventId);

    List<ParticipationRequestDto> getUserRequests(long userId);

    ParticipationRequestDto cancel(long userId, long requestId);

    List<ParticipationRequestDto> getEventRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventRequests(long userId,
                                                       long eventId,
                                                       EventRequestStatusUpdateRequest request);
}