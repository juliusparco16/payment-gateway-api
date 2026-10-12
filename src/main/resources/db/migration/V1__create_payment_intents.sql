CREATE TABLE payment_intents (
    id UUID PRIMARY KEY,
    owner_subject VARCHAR(100) NOT NULL,
    amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
    currency VARCHAR(3) NOT NULL CHECK (currency = UPPER(currency)),
    description VARCHAR(255),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payment_intents_owner_subject
    ON payment_intents (owner_subject);
