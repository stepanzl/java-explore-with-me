package ru.practicum.main.events.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.main.events.model.Event;
import ru.practicum.main.events.model.EventState;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long id, EventState state);
}