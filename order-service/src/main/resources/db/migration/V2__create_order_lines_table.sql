CREATE TABLE order_lines (
    id           UUID PRIMARY KEY,
    order_id     UUID NOT NULL REFERENCES orders(id),
    sku          VARCHAR(50) NOT NULL,
    quantity     INTEGER NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_order_lines_order_id ON order_lines(order_id);