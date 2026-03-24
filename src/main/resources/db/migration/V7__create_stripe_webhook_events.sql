CREATE TABLE stripe_webhook_events (
    event_id TEXT PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL
);
