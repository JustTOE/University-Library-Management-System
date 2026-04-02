CREATE TABLE book(
    id                  SERIAL          PRIMARY KEY,
    title               VARCHAR(255)    NOT NULL,
    author              VARCHAR(255),
    isbn                VARCHAR(20)     NOT NULL UNIQUE,
    publication_year    INT,
    subject             VARCHAR(255)    NOT NULL,
    total_copies        INT             NOT NULL DEFAULT 1 CHECK ( total_copies >= 0 ),
    available_copies    INT             NOT NULL,
    shelf_number        VARCHAR(100)    NOT NULL,
    CONSTRAINT chk_available_lte_total
        CHECK (available_copies <= total_copies)
);

CREATE INDEX idx_book_isbn ON book(isbn);
