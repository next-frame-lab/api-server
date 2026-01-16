CREATE
EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS payments
(
    id
    uuid
    DEFAULT
    gen_random_uuid
(
) NOT NULL PRIMARY KEY,
    reservation_id uuid UNIQUE,
    total_amount integer,
    requested_at timestamp,
    payment_method varchar,
    status varchar DEFAULT 'REQUESTED'
    );

CREATE TABLE IF NOT EXISTS ticket_issue_outbox
(
    id
    uuid
    DEFAULT
    gen_random_uuid
(
) NOT NULL PRIMARY KEY,
    payment_id uuid NOT NULL,
    reservation_id uuid NOT NULL UNIQUE,
    ticket_id uuid,
    status varchar NOT NULL DEFAULT 'PENDING',
    retry_count int NOT NULL DEFAULT 0,
    next_retry_at timestamp NOT NULL DEFAULT now
(
),
    last_error varchar,
    created_at timestamp NOT NULL DEFAULT now
(
),
    updated_at timestamp NOT NULL DEFAULT now
(
)
    );

create table reservations
(
    id uuid primary key
);