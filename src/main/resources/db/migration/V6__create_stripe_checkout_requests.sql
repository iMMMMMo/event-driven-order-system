CREATE TABLE stripe_checkout_requests (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    checkout_url TEXT,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT,
    CONSTRAINT fk_stripe_checkout_requests_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_stripe_checkout_requests_order_id ON stripe_checkout_requests(order_id);
CREATE INDEX idx_stripe_checkout_requests_status ON stripe_checkout_requests(status);
