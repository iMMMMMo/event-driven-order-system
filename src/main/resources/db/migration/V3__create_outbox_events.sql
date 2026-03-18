CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (occurred_at)
    WHERE published_at IS NULL;

CREATE INDEX idx_outbox_events_published_at
    ON outbox_events (published_at);