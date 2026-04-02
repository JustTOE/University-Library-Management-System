CREATE TABLE notification (
    id         SERIAL           PRIMARY KEY,
    sent_date  TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    message    TEXT             NOT NULL,
    type       VARCHAR(50)      NOT NULL DEFAULT 'GENERAL'
        CHECK (type IN ('DUE_REMINDER', 'OVERDUE_ALERT', 'RESERVATION_READY', 'GENERAL')),
    loan_id    INT,
    user_id    INT              NOT NULL,
    status     VARCHAR(50)      NOT NULL DEFAULT 'NOT_ACKNOWLEDGED'
        CHECK (status IN ('ACKNOWLEDGED', 'NOT_ACKNOWLEDGED')),
    CONSTRAINT fk_notification_loan
        FOREIGN KEY (loan_id) REFERENCES loan(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_notification_loan ON notification(loan_id);
CREATE INDEX idx_notification_user ON notification(user_id);
