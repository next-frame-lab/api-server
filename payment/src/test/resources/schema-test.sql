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

CREATE TABLE IF NOT EXISTS refunds
(
    id uuid PRIMARY KEY,
    payment_id uuid NOT NULL,
    refund_amount integer NOT NULL,
    status varchar NOT NULL,
    reason varchar,
    refund_policy varchar,
    requested_at timestamp,
    completed_at timestamp
);

CREATE TABLE IF NOT EXISTS schedules
(
    id uuid PRIMARY KEY,
    performance_datetime timestamp NOT NULL
);

create table reservations
(
    id uuid primary key,
    schedule_id uuid
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