CREATE TABLE order_events (
    id CHAR(36) NOT NULL PRIMARY KEY,
    order_id CHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_json JSON NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_order_events_order_id FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_order_events_order_id ON order_events(order_id);