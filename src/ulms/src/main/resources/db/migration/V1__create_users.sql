CREATE TABLE users (
    id              SERIAL          PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    university_id   VARCHAR(100)    UNIQUE,
    staff_id        VARCHAR(100)    UNIQUE,
    phone           VARCHAR(30),
    role            VARCHAR(20)     NOT NULL
        CHECK (role IN ('STUDENT', 'LIBRARIAN', 'ADMIN')),
    password_hash   VARCHAR(255)    NOT NULL
);
