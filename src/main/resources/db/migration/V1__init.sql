create schema if not exists router;

create type router.provider_status as enum ('ACTIVE', 'DISABLED');

create table if not exists router.providers (
  id text primary key,
  name text not null,
  base_url text null,
  status router.provider_status not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists router.models (
  id text not null,
  provider_id text not null,
  name text not null,
  max_context_tokens integer not null,
  price_prompt_per_1k numeric(12,6) null,
  price_completion_per_1k numeric(12,6) null,
  capabilities jsonb null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (provider_id, id),
  constraint models_provider_fk foreign key (provider_id) references router.providers(id) on delete cascade
);

create table if not exists router.routing_decisions (
  request_id text primary key,
  user_id text not null,
  conversation_id text not null,
  provider_id text not null,
  model_id text not null,
  reason text null,
  candidates jsonb null,
  latency_ms integer null,
  prompt_tokens integer null,
  completion_tokens integer null,
  total_tokens integer null,
  estimated_cost numeric(12,6) null,
  currency text null,
  created_at timestamptz not null default now()
);

create index if not exists routing_decisions_user_created_at_idx
  on router.routing_decisions (user_id, created_at desc);
