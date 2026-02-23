package ru.practicum.main.events.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.main.categories.model.Category;
import ru.practicum.main.users.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events",
        indexes = {
                @Index(name = "idx_event_date", columnList = "event_date"),
                @Index(name = "idx_event_category", columnList = "category_id"),
                @Index(name = "idx_event_state", columnList = "state"),
                @Index(name = "idx_event_initiator", columnList = "initiator_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"category", "initiator"})
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(nullable = false)
    @Builder.Default
    private Boolean paid = false;

    @Column(name = "participant_limit", nullable = false)
    @Builder.Default
    private Integer participantLimit = 0;

    @Column(name = "request_moderation", nullable = false)
    @Builder.Default
    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_initiator"))
    private User initiator;

    @Embedded
    private Location location;
}