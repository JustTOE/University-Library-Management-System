CREATE TABLE student(
    id              SERIAL          PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    university_id   VARCHAR(100)    NOT NULL UNIQUE,
    phone           VARCHAR(30),
    password_hash   VARCHAR(255)    NOT NULL
);