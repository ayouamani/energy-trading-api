CREATE TABLE trades (
    id                UUID           PRIMARY KEY,
    portfolio         VARCHAR(64)    NOT NULL,
    commodity         VARCHAR(16)    NOT NULL,
    direction         VARCHAR(8)     NOT NULL,
    quantity_mwh      NUMERIC(19, 4) NOT NULL,
    price_eur_per_mwh NUMERIC(19, 4) NOT NULL,
    delivery_date     DATE           NOT NULL,
    status            VARCHAR(16)    NOT NULL,
    booked_at         TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_trades_portfolio ON trades (portfolio);

CREATE TABLE positions (
    id               BIGSERIAL      PRIMARY KEY,
    portfolio        VARCHAR(64)    NOT NULL,
    commodity        VARCHAR(16)    NOT NULL,
    delivery_date    DATE           NOT NULL,
    net_quantity_mwh NUMERIC(19, 4) NOT NULL,
    notional_eur     NUMERIC(19, 4) NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_positions_book UNIQUE (portfolio, commodity, delivery_date)
);

-- Consumer-side idempotency: an event id is applied at most once.
CREATE TABLE processed_events (
    event_id     UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
