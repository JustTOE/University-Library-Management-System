CREATE TABLE notification (
    id         SERIAL           PRIMARY KEY,
    sent_date  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    message    TEXT             NOT NULL,
    type       VARCHAR(50)      NOT NULL DEFAULT 'GENERAL'
        CHECK (type IN ('DUE_REMINDER', 'OVERDUE_ALERT', 'RESERVATION_READY', 'GENERAL')),
    loan_id    INT,
    student_id INT              NOT NULL,
    status     VARCHAR(50)      NOT NULL DEFAULT 'NOT_ACKNOWLEDGED'
        CHECK (status IN ('ACKNOWLEDGED', 'NOT_ACKNOWLEDGED')),
    CONSTRAINT fk_notification_loan
        FOREIGN KEY (loan_id) REFERENCES loan(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_notification_student
        FOREIGN KEY (student_id) REFERENCES student(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_notification_loan ON notification(loan_id);
CREATE INDEX idx_notification_student ON notification(student_id);