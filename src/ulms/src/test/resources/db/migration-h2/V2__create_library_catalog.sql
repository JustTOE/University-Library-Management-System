CREATE TABLE library_catalog(
    id              SERIAL  PRIMARY KEY,
    total_books     INT     NOT NULL DEFAULT 0,
    last_updated    DATE    NOT NULL DEFAULT CURRENT_DATE
);
