CREATE TABLE audit_log (
    id           BIGSERIAL    PRIMARY KEY,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    action       VARCHAR(40)  NOT NULL,
    actor_id     INT          NULL,
    actor_email  VARCHAR(255) NULL,
    target_type  VARCHAR(40)  NULL,
    target_id    VARCHAR(40)  NULL,
    detail       TEXT         NULL
);

CREATE INDEX idx_audit_log_occurred_at ON audit_log(occurred_at DESC);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_id, occurred_at DESC);
CREATE INDEX idx_audit_log_action ON audit_log(action, occurred_at DESC);
