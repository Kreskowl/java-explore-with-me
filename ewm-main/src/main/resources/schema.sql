CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS event (
    id BIGSERIAL PRIMARY KEY,
        title VARCHAR(120) NOT NULL,
        annotation TEXT NOT NULL,
        description TEXT NOT NULL,
        category_id BIGINT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
        confirmed_requests INTEGER DEFAULT 0,
        created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        event_date TIMESTAMP NOT NULL,
        published_on TIMESTAMP,
        initiator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        lat FLOAT NOT NULL,
        lon FLOAT NOT NULL,
        paid BOOLEAN NOT NULL DEFAULT FALSE,
        participant_limit INTEGER DEFAULT 0,
        request_moderation BOOLEAN,
        state VARCHAR,
        views BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS participation_request (
    id BIGSERIAL PRIMARY KEY,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_id BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    requester_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    event_id BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_by_admin BOOLEAN
);

CREATE TABLE IF NOT EXISTS compilation (
    id BIGSERIAL PRIMARY KEY,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    title VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS compilation_event (
    compilation_id BIGINT NOT NULL REFERENCES compilation(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_event_event_date ON event (event_date);
CREATE INDEX IF NOT EXISTS idx_event_state ON event (state);
CREATE INDEX IF NOT EXISTS idx_request_event_id ON participation_request (event_id);
CREATE INDEX IF NOT EXISTS idx_request_requester_id ON participation_request (requester_id);
CREATE INDEX IF NOT EXISTS idx_event_initiator_id ON event (initiator_id);
CREATE INDEX IF NOT EXISTS idx_event_category_id ON event (category_id);
CREATE INDEX IF NOT EXISTS idx_comments_event_id ON comments (event_id);
CREATE INDEX IF NOT EXISTS idx_comments_author_id ON comments (author_id);