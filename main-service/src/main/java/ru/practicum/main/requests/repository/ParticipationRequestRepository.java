package ru.practicum.main.requests.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.requests.model.ParticipationRequest;
import ru.practicum.main.requests.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long id, Long requesterId);

    @EntityGraph(attributePaths = {"event", "requester"})
    List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    List<ParticipationRequest> findAllByIdInAndEventIdAndEventInitiatorId(List<Long> ids, Long eventId, Long initiatorId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

}