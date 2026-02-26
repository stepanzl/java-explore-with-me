DROP TABLE IF EXISTS compilation_events CASCADE;
DROP TABLE IF EXISTS participation_requests CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS location_areas CASCADE;
DROP TABLE IF EXISTS users CASCADE;

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

CREATE TABLE IF NOT EXISTS location_areas
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255)                NOT NULL,
    type       VARCHAR(50)                 NOT NULL,
    lat        NUMERIC(10, 7)              NOT NULL,
    lon        NUMERIC(10, 7)              NOT NULL,
    radius_km  NUMERIC(10, 3)              NOT NULL,
    created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL
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

CREATE INDEX IF NOT EXISTS idx_location_area_type ON location_areas (type);
CREATE INDEX IF NOT EXISTS idx_location_area_name ON location_areas (name);

CREATE OR REPLACE FUNCTION distance(lat1 float, lon1 float, lat2 float, lon2 float)
    RETURNS float
AS
'
declare
    dist float = 0;
    rad_lat1 float;
    rad_lat2 float;
    theta float;
    rad_theta float;
BEGIN
    IF lat1 = lat2 AND lon1 = lon2
    THEN
        RETURN dist;
    ELSE
        rad_lat1 = pi() * lat1 / 180;
        rad_lat2 = pi() * lat2 / 180;
        theta = lon1 - lon2;
        rad_theta = pi() * theta / 180;
        dist = sin(rad_lat1) * sin(rad_lat2) + cos(rad_lat1) * cos(rad_lat2) * cos(rad_theta);

        IF dist > 1
            THEN dist = 1;
        END IF;

        dist = acos(dist);
        dist = dist * 180 / pi();
        dist = dist * 60 * 1.8524;

        RETURN dist;
    END IF;
END;
'
LANGUAGE PLPGSQL;