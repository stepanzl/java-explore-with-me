CREATE TABLE IF NOT EXISTS users
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email)
);


CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);


CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,

    annotation         VARCHAR(2000)               NOT NULL,
    description        TEXT                        NOT NULL,
    title              VARCHAR(255)                NOT NULL,

    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_on       TIMESTAMP WITHOUT TIME ZONE,

    paid               BOOLEAN                     NOT NULL DEFAULT FALSE,
    participant_limit  INT                         NOT NULL DEFAULT 0,
    request_moderation BOOLEAN                     NOT NULL DEFAULT TRUE,

    state              VARCHAR(50)                 NOT NULL,

    category_id        BIGINT                      NOT NULL,
    initiator_id       BIGINT                      NOT NULL,

    lat                NUMERIC(10, 7)              NOT NULL,
    lon                NUMERIC(10, 7)              NOT NULL,

    CONSTRAINT fk_event_category
        FOREIGN KEY (category_id)
            REFERENCES categories (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_event_initiator
        FOREIGN KEY (initiator_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS participation_requests
(
    id           BIGSERIAL PRIMARY KEY,

    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status       VARCHAR(50)                 NOT NULL,

    event_id     BIGINT                      NOT NULL,
    requester_id BIGINT                      NOT NULL,

    CONSTRAINT fk_request_event
        FOREIGN KEY (event_id)
            REFERENCES events (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_request_user
        FOREIGN KEY (requester_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_request UNIQUE (event_id, requester_id)
);


CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGSERIAL PRIMARY KEY,
    title  VARCHAR(255) NOT NULL,
    pinned BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_compilation_title UNIQUE (title)
);


CREATE TABLE IF NOT EXISTS compilation_events
(
    comp_id  BIGINT NOT NULL,
    event_id BIGINT NOT NULL,

    PRIMARY KEY (comp_id, event_id),

    CONSTRAINT fk_comp_event_comp
        FOREIGN KEY (comp_id)
            REFERENCES compilations (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_comp_event_event
        FOREIGN KEY (event_id)
            REFERENCES events (id)
            ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_event_date ON events (event_date);
CREATE INDEX IF NOT EXISTS idx_event_category ON events (category_id);
CREATE INDEX IF NOT EXISTS idx_event_state ON events (state);
CREATE INDEX IF NOT EXISTS idx_event_initiator ON events (initiator_id);

CREATE INDEX IF NOT EXISTS idx_compilation_pinned ON compilations (pinned);