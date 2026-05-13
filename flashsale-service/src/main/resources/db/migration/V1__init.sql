CREATE TABLE flash_sale_slots (
	id UUID PRIMARY KEY,
	sale_date DATE NOT NULL,
	start_time TIME NOT NULL,
	end_time TIME NOT NULL,
	active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE flash_sale_products (
	id UUID PRIMARY KEY,
	slot_id UUID NOT NULL REFERENCES flash_sale_slots (id) ON DELETE CASCADE,
	product_name VARCHAR(255) NOT NULL,
	description TEXT NOT NULL,
	original_price NUMERIC(19, 2) NOT NULL,
	flash_price NUMERIC(19, 2) NOT NULL,
	total_quantity INTEGER NOT NULL,
	remaining_quantity INTEGER NOT NULL,
	version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_flash_sale_products_slot_id ON flash_sale_products (slot_id);

CREATE TABLE purchase_orders (
	id UUID PRIMARY KEY,
	user_id UUID NOT NULL,
	flash_sale_product_id UUID NOT NULL REFERENCES flash_sale_products (id),
	amount NUMERIC(19, 2) NOT NULL,
	status VARCHAR(20) NOT NULL,
	purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_purchase_orders_user_purchased_at ON purchase_orders (user_id, purchased_at);

