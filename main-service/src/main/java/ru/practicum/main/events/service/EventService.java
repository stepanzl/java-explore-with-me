package ru.practicum.main.events.service;

import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventAdminRequest;
import ru.practicum.main.events.dto.UpdateEventUserRequest;

import java.util.List;

public interface EventService {

    EventFullDto createPrivate(long userId, NewEventDto dto);

    List<EventShortDto> getPrivateEvents(long userId, int from, int size);

    EventFullDto getPrivateEventById(long userId, long eventId);

    EventFullDto updatePrivate(long userId, long eventId, UpdateEventUserRequest dto);

    List<EventFullDto> searchAdmin(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd,
            int from,
            int size
    );

    EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest dto);
}