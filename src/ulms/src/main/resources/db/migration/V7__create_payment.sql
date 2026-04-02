CREATE TABLE payment (
    id            SERIAL            PRIMARY KEY,
    amount        NUMERIC(10, 2)    NOT NULL CHECK (amount >= 0),
    payment_date  TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_DATE,
    method        VARCHAR(50)       NOT NULL
        CHECK (method IN ('CASH', 'CARD', 'ONLINE', 'OTHER')),
    status        VARCHAR(50)       NOT NULL DEFAULT 'COMPLETED'
        CHECK (status IN ('COMPLETED', 'PENDING', 'FAILED', 'REFUNDED')),
    fine_id       INT               NOT NULL,
    user_id       INT               NOT NULL,
    CONSTRAINT fk_payment_fine
        FOREIGN KEY (fine_id) REFERENCES fine(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_payment_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_payment_fine ON payment(fine_id);
CREATE INDEX idx_payment_user ON payment(user_id);
