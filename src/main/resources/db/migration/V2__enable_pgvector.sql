create extension if not exists vector schema public;

create table if not exists mc_warehouse_service.vector_store
(
    id        uuid primary key,
    content   text,
    metadata  jsonb,
    embedding vector(1536) not null
);

create index on mc_warehouse_service.vector_store using hnsw (embedding vector_cosine_ops);
