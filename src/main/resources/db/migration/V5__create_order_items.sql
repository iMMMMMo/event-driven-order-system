CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    line_total NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT
);

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order_id
        FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_product_id
        FOREIGN KEY (product_id) REFERENCES inventory_items (id);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);