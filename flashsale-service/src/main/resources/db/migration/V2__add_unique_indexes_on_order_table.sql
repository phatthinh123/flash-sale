CREATE UNIQUE INDEX uq_purchase_orders_user_per_day
    ON purchase_orders (user_id, (DATE(purchased_at)));

