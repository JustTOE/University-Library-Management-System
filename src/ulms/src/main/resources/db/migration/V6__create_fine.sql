CREATE TABLE fine (
    id                SERIAL          PRIMARY KEY,
    amount            NUMERIC(10, 2)  NOT NULL CHECK (amount >= 0),
    fine_id           VARCHAR(50)     NOT NULL UNIQUE,
    calculated_date   DATE            NOT NULL DEFAULT CURRENT_DATE,
    status            VARCHAR(50)     NOT NULL DEFAULT 'UNPAID'
        CHECK (status IN ('UNPAID', 'PAID', 'WAIVED')),
    loan_id           INT             NOT NULL,
    CONSTRAINT fk_fine_loan
        FOREIGN KEY (loan_id) REFERENCES loan(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_fine_loan   ON fine(loan_id);
CREATE INDEX idx_fine_status ON fine(status);