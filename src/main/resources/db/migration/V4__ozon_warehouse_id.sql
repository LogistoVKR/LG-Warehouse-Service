alter table mc_warehouse_service.point_of_storage
    add column if not exists ozon_warehouse_id bigint;

create unique index if not exists idx__point_of_storage__ozon_warehouse_id
    on mc_warehouse_service.point_of_storage (organization_id, ozon_warehouse_id)
    where ozon_warehouse_id is not null;
