-- Initial schema for inventory-service

CREATE TABLE inventory (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID          NOT NULL UNIQUE,
    product_name    VARCHAR(255)  NOT NULL,
    stock_quantity  INTEGER       NOT NULL DEFAULT 0,
    last_synced_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inventory_product_id ON inventory (product_id);

-- Tracks processed order IDs to guarantee idempotent event handling
CREATE TABLE processed_orders (
    order_id       VARCHAR(255) PRIMARY KEY,
    processed_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

