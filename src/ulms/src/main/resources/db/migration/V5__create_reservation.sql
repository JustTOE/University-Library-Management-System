CREATE TABLE reservation(
    id                  SERIAL          PRIMARY KEY,
    reserved_at         TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_DATE,
    expiry_date         DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
        CHECK ( status IN ('ACTIVE', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    user_id             INT             NOT NULL,
    book_id             INT,
    CONSTRAINT fk_reservation_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_reservation_book
        FOREIGN KEY (book_id) REFERENCES book(id)
            ON DELETE SET NULL
);

CREATE INDEX idx_reservation_user ON reservation(user_id);
CREATE INDEX idx_reservation_book ON reservation(book_id);
