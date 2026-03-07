CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    version BIGINT
);

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    product_name VARCHAR(255) UNIQUE NOT NULL,
    quantity INTEGER NOT NULL,
    reserved INTEGER NOT NULL,
    version BIGINT
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);