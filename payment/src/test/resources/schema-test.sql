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
CREATE TABLE reservation_cancel_outbox
(
    id              uuid      DEFAULT gen_random_uuid() NOT NULL PRIMARY KEY,
    reservation_id  uuid                                NOT NULL UNIQUE, -- 취소 대상 식별
    payment_id      uuid                                NOT NULL,        -- 이력 추적용
    status          varchar   DEFAULT 'PENDING'         NOT NULL,        -- PENDING, PROCESSED, FAILED
    retry_count     integer   DEFAULT 0                 NOT NULL,
    next_retry_at   timestamp DEFAULT NOW()             NOT NULL,
    last_error      text,                                                -- 에러 메시지 (길이 고려 text)
    created_at      timestamp DEFAULT NOW()             NOT NULL,
    updated_at      timestamp DEFAULT NOW()             NOT NULL
);