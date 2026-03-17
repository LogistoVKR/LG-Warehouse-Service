create schema if not exists mc_warehouse_service;

create table if not exists mc_warehouse_service.item (
    id              uuid primary key,
    name            varchar(255) not null,
    description     varchar(255),
    organization_id uuid         not null
);

create index if not exists idx__item__organization_id on mc_warehouse_service.item (organization_id);
create index if not exists idx__item__name on mc_warehouse_service.item (name);

create table if not exists mc_warehouse_service.item_variant (
    id       uuid primary key,
    sku      varchar(255)   not null,
    barcode  varchar(255),
    item_id  uuid           not null references mc_warehouse_service.item (id) not null,
    price    decimal(10, 2) not null,
    currency varchar(3)     not null
);

create index if not exists idx__item_variant__item_id on mc_warehouse_service.item_variant (item_id);
create index if not exists idx__item_variant__sku on mc_warehouse_service.item_variant (sku);
create index if not exists idx__item_variant__barcode on mc_warehouse_service.item_variant (barcode);

create type mc_warehouse_service.point_of_storage_type as enum ('WAREHOUSE', 'POINT_OF_SALE');

create table if not exists mc_warehouse_service.point_of_storage (
    id              uuid primary key,
    name            varchar(255)                               not null,
    description     varchar(255),
    location        varchar(255),
    type            mc_warehouse_service.point_of_storage_type not null,
    organization_id uuid                                       not null
);

create index if not exists idx__point_of_storage__organization_id on mc_warehouse_service.point_of_storage (organization_id);
create index if not exists idx__point_of_storage__name on mc_warehouse_service.point_of_storage (name);
create index if not exists idx__point_of_storage__location on mc_warehouse_service.point_of_storage (location);

create table if not exists mc_warehouse_service.item_variant_point_of_storage (
    point_of_storage_id uuid    not null references mc_warehouse_service.point_of_storage (id) not null,
    item_variant_id     uuid    not null references mc_warehouse_service.item_variant (id) not null,
    quantity            integer not null,
    reserved            integer not null,
    unique (point_of_storage_id, item_variant_id)
);

create index if not exists idx__item_variant_point_of_storage__point_of_storage_id on mc_warehouse_service.item_variant_point_of_storage (point_of_storage_id);
create index if not exists idx__item_variant_point_of_storage__item_variant_id on mc_warehouse_service.item_variant_point_of_storage (item_variant_id);

create type mc_warehouse_service.movement_type as enum ('PURCHASE','SALE','TRANSFER','RETURN','WRITE_OFF','RESERVE');

create table if not exists mc_warehouse_service.item_variant_movement (
    id                       uuid primary key,
    from_point_of_storage_id uuid references mc_warehouse_service.point_of_storage (id),
    to_point_of_storage_id   uuid references mc_warehouse_service.point_of_storage (id),
    item_variant_id          uuid references mc_warehouse_service.item_variant (id) not null,
    price_per_item           decimal(10, 2),
    currency                 varchar(3),
    quantity                 integer                                                not null check (quantity > 0),
    reason                   varchar(255),
    type                     mc_warehouse_service.movement_type                     not null,
    created                  timestamp                                              not null,
    organization_id          uuid                                                   not null
);

create index if not exists idx__item_variant_movement__from_point_of_storage_id on mc_warehouse_service.item_variant_movement (from_point_of_storage_id);
create index if not exists idx__item_variant_movement__to_point_of_storage_id on mc_warehouse_service.item_variant_movement (to_point_of_storage_id);
create index if not exists idx__item_variant_movement__item_variant_id on mc_warehouse_service.item_variant_movement (item_variant_id);
create index if not exists idx__item_variant_movement__created on mc_warehouse_service.item_variant_movement (created);
create index if not exists idx__item_variant_movement__type on mc_warehouse_service.item_variant_movement (type);
create index if not exists idx__item_variant_movement__organization_id on mc_warehouse_service.item_variant_movement (organization_id);
