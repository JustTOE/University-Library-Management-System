CREATE TABLE loan (
    id              SERIAL PRIMARY KEY,
    borrow_date     DATE         NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE         NOT NULL,
    return_date     DATE,
    loan_id         VARCHAR(50)  NOT NULL UNIQUE,
    renewal_count   INT          NOT NULL DEFAULT 0 CHECK (renewal_count >= 0),
    status          VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'RETURNED', 'RENEWED','OVERDUE', 'LOST')),
    user_id         INT          NOT NULL,
    book_id         INT,
    CONSTRAINT fk_loan_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_loan_book
        FOREIGN KEY (book_id) REFERENCES book(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_loan_user ON loan(user_id);
CREATE INDEX idx_loan_book ON loan(book_id);
CREATE INDEX idx_loan_status ON loan(status);
