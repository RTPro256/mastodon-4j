create table if not exists jobs (
    id bigserial primary key,
    queue varchar(64) not null,
    payload text,
    run_at timestamptz not null,
    attempts integer not null default 0,
    max_attempts integer not null default 5,
    last_error text,
    locked_at timestamptz,
    locked_by varchar(64),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index if not exists idx_jobs_queue_run_at on jobs(queue, run_at);
create index if not exists idx_jobs_locked_at on jobs(locked_at);
