ALTER TABLE mc_warehouse_service.item_variant_movement
    ADD COLUMN client_id UUID,
    ADD COLUMN discount  DECIMAL(5, 2);
