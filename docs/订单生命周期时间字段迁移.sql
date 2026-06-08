ALTER TABLE order_main
    ADD COLUMN guest_registered_at DATETIME NULL COMMENT 'guest registration saved time' AFTER updated_at,
    ADD COLUMN checked_out_at DATETIME NULL COMMENT 'checkout operation time' AFTER guest_registered_at;
