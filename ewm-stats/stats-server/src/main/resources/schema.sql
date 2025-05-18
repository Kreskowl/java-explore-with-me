CREATE TABLE IF NOT EXISTS endpoint_hits (
    id BIGSERIAL NOT NULL,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    ip VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT pk_endpoint_hit_id PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_endpoint_hits_uri_time
    ON endpoint_hits (uri, timestamp);
