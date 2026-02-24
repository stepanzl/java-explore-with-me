package ru.practicum.main.events.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.categories.model.Category;
import ru.practicum.main.categories.repository.CategoryRepository;
import ru.practicum.main.events.dto.EventFullDto;
import ru.practicum.main.events.dto.EventShortDto;
import ru.practicum.main.events.dto.NewEventDto;
import ru.practicum.main.events.dto.UpdateEventAdminRequest;
import ru.practicum.main.events.dto.UpdateEventUserRequest;
import ru.practicum.main.events.mapper.EventMapper;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventState;
import ru.practicum.main.events.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.users.model.User;
import ru.practicum.main.users.repository.UserRepository;
import ru.practicum.main.util.PageRequestUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    private static final String USER_SEND_TO_REVIEW = "SEND_TO_REVIEW";
    private static final String USER_CANCEL_REVIEW = "CANCEL_REVIEW";

    private static final String ADMIN_PUBLISH_EVENT = "PUBLISH_EVENT";
    private static final String ADMIN_REJECT_EVENT = "REJECT_EVENT";

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventFullDto createPrivate(long userId, NewEventDto dto) {
        log.info("Create private event: userId={}", userId);

        User initiator = getUser(userId);
        Category category = getCategory(dto.getCategory());

        Event event = eventMapper.toEntity(dto);

        if (event.getEventDate() == null) {
            throw new BadRequestException("eventDate must not be null");
        }
        validateEventDate(event.getEventDate());

        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventShortDto> getPrivateEvents(long userId, int from, int size) {
        log.info("Get private events: userId={}, from={}, size={}", userId, from, size);
        getUser(userId);

        PageRequest pageable = PageRequestUtil.from(from, size);
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto getPrivateEventById(long userId, long eventId) {
        log.info("Get private event: userId={}, eventId={}", userId, eventId);
        getUser(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updatePrivate(long userId, long eventId, UpdateEventUserRequest dto) {
        log.info("Update private event: userId={}, eventId={}", userId, eventId);
        getUser(userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        eventMapper.updateFromUser(dto, event);

        if (dto.getCategory() != null) {
            event.setCategory(getCategory(dto.getCategory()));
        }

        if (event.getEventDate() != null && dto.getEventDate() != null) {
            validateEventDate(event.getEventDate());
        }

        if (dto.getStateAction() != null) {
            applyUserStateAction(event, dto.getStateAction());
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventFullDto> searchAdmin(List<Long> users,
                                          List<String> states,
                                          List<Long> categories,
                                          String rangeStart,
                                          String rangeEnd,
                                          int from,
                                          int size) {
        log.info("Search admin events: from={}, size={}", from, size);

        Specification<Event> spec = buildAdminSpec(users, states, categories, rangeStart, rangeEnd);
        PageRequest pageable = PageRequestUtil.from(from, size);

        return eventRepository.findAll(spec, pageable)
                .stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdmin(long eventId, UpdateEventAdminRequest dto) {
        log.info("Update admin event: eventId={}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        eventMapper.updateFromAdmin(dto, event);

        if (dto.getCategory() != null) {
            event.setCategory(getCategory(dto.getCategory()));
        }

        if (event.getEventDate() != null && dto.getEventDate() != null) {
            validateEventDate(event.getEventDate());
        }

        if (dto.getStateAction() != null) {
            applyAdminStateAction(event, dto.getStateAction());
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    private void applyUserStateAction(Event event, String stateAction) {
        if (USER_SEND_TO_REVIEW.equals(stateAction)) {
            event.setState(EventState.PENDING);
            return;
        }
        if (USER_CANCEL_REVIEW.equals(stateAction)) {
            event.setState(EventState.CANCELED);
            return;
        }
        throw new BadRequestException("Unknown stateAction: " + stateAction);
    }

    private void applyAdminStateAction(Event event, String stateAction) {
        if (ADMIN_PUBLISH_EVENT.equals(stateAction)) {
            publish(event);
            return;
        }
        if (ADMIN_REJECT_EVENT.equals(stateAction)) {
            reject(event);
            return;
        }
        throw new BadRequestException("Unknown stateAction: " + stateAction);
    }

    private void publish(Event event) {
        if (event.getState() != EventState.PENDING) {
            throw new ConflictException("Event can be published only if it is in PENDING state");
        }
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void reject(Event event) {
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Event can be rejected only if it is not published");
        }
        event.setState(EventState.CANCELED);
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            throw new BadRequestException("Event date must be at least " + MIN_HOURS_BEFORE_EVENT + " hours in the future");
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategory(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Specification<Event> buildAdminSpec(List<Long> users,
                                                List<String> states,
                                                List<Long> categories,
                                                String rangeStart,
                                                String rangeEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }

            if (states != null && !states.isEmpty()) {
                List<EventState> parsed = states.stream()
                        .map(this::parseState)
                        .toList();
                predicates.add(root.get("state").in(parsed));
            }

            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }

            LocalDateTime start = null;
            LocalDateTime end = null;

            if (rangeStart != null && !rangeStart.isBlank()) {
                start = ru.practicum.main.util.DateTimeFormat.FORMATTER.parse(rangeStart, LocalDateTime::from);
            }
            if (rangeEnd != null && !rangeEnd.isBlank()) {
                end = ru.practicum.main.util.DateTimeFormat.FORMATTER.parse(rangeEnd, LocalDateTime::from);
            }

            if (start != null && end != null && end.isBefore(start)) {
                throw new BadRequestException("rangeEnd must not be before rangeStart");
            }

            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), start));
            }

            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private EventState parseState(String state) {
        try {
            return EventState.valueOf(state);
        } catch (Exception ex) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}