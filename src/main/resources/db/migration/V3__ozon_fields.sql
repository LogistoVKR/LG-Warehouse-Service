ALTER TABLE mc_warehouse_service.item
    ADD COLUMN ozon_model_id BIGINT;

ALTER TABLE mc_warehouse_service.item_variant
    ADD COLUMN ozon_product_id BIGINT;

CREATE UNIQUE INDEX idx__item__ozon_model_id
    ON mc_warehouse_service.item (organization_id, ozon_model_id)
    WHERE ozon_model_id IS NOT NULL;

CREATE UNIQUE INDEX idx__item_variant__ozon_product_id
    ON mc_warehouse_service.item_variant (ozon_product_id)
    WHERE ozon_product_id IS NOT NULL;
