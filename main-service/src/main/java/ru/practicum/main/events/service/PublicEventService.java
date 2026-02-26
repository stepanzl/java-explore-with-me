package ru.practicum.main.events.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.PublicEventSort;

import java.util.List;

public interface PublicEventService {

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        PublicEventSort sort,
                                        Long locationId,
                                        int from,
                                        int size,
                                        HttpServletRequest request);

    EventFullDto getPublicEvent(long eventId, HttpServletRequest request);
}