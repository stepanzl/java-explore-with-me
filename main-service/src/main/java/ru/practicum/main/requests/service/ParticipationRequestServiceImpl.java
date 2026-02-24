package ru.practicum.main.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventState;
import ru.practicum.main.events.repository.EventRepository;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.requests.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.requests.dto.ParticipationRequestDto;
import ru.practicum.main.requests.dto.RequestStatusUpdateAction;
import ru.practicum.main.requests.mapper.ParticipationRequestMapper;
import ru.practicum.main.requests.model.ParticipationRequest;
import ru.practicum.main.requests.model.RequestStatus;
import ru.practicum.main.requests.repository.ParticipationRequestRepository;
import ru.practicum.main.users.model.User;
import ru.practicum.main.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto create(long userId, long eventId) {
        log.info("Create participation request: userId={}, eventId={}", userId, eventId);

        User requester = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot create request for own event");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        if (limit > 0) {
            long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmed >= limit) {
                throw new ConflictException("Participant limit reached");
            }
        }

        ParticipationRequest pr = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(resolveInitialStatus(event))
                .build();

        ParticipationRequest saved = requestRepository.save(pr);
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(long userId) {
        log.info("Get user requests: userId={}", userId);
        getUser(userId);

        return requestMapper.toDtoList(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(long userId, long requestId) {
        log.info("Cancel request: userId={}, requestId={}", userId, requestId);
        getUser(userId);

        ParticipationRequest pr = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        pr.setStatus(RequestStatus.CANCELED);
        ParticipationRequest saved = requestRepository.save(pr);
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(long userId, long eventId) {
        log.info("Get event requests: userId={}, eventId={}", userId, eventId);
        getUser(userId);
        getEventOwnedByUser(eventId, userId);

        return requestMapper.toDtoList(requestRepository.findAllByEventIdAndEventInitiatorId(eventId, userId));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(long userId,
                                                              long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Update event requests: userId={}, eventId={}, action={}", userId, eventId, request.getStatus());
        getUser(userId);

        Event event = getEventOwnedByUser(eventId, userId);

        List<Long> ids = request.getRequestIds();
        List<ParticipationRequest> requests = requestRepository
                .findAllByIdInAndEventIdAndEventInitiatorId(ids, eventId, userId);

        if (requests.size() != ids.size()) {
            throw new NotFoundException("One or more participation requests were not found");
        }

        RequestStatusUpdateAction action = request.getStatus();

        long limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        long confirmedCount = (limit > 0)
                ? requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)
                : 0;

        if (action == RequestStatusUpdateAction.CONFIRMED && limit > 0 && confirmedCount >= limit) {
            throw new ConflictException("Participant limit reached");
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        for (ParticipationRequest pr : requests) {
            if (pr.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Only PENDING requests can be updated");
            }
        }

        if (action == RequestStatusUpdateAction.REJECTED) {
            for (ParticipationRequest pr : requests) {
                pr.setStatus(RequestStatus.REJECTED);
                rejected.add(pr);
            }
            requestRepository.saveAll(requests);
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requestMapper.toDtoList(confirmed))
                    .rejectedRequests(requestMapper.toDtoList(rejected))
                    .build();
        }

        // CONFIRMED action
        for (ParticipationRequest pr : requests) {
            if (limit == 0 || confirmedCount < limit) {
                pr.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(pr);
                confirmedCount++;
            } else {
                pr.setStatus(RequestStatus.REJECTED);
                rejected.add(pr);
            }
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestMapper.toDtoList(confirmed))
                .rejectedRequests(requestMapper.toDtoList(rejected))
                .build();
    }

    private RequestStatus resolveInitialStatus(Event event) {
        long limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
        boolean moderation = Boolean.TRUE.equals(event.getRequestModeration());

        if (!moderation || limit == 0) {
            return RequestStatus.CONFIRMED;
        }
        return RequestStatus.PENDING;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getEventOwnedByUser(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}