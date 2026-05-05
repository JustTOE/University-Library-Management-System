CREATE TABLE scheduler_run (
    id               SERIAL          PRIMARY KEY,
    job_name         VARCHAR(100)    NOT NULL,
    started_at       TIMESTAMPTZ     NOT NULL,
    finished_at      TIMESTAMPTZ,
    items_processed  INT             NOT NULL DEFAULT 0,
    status           VARCHAR(10)     NOT NULL CHECK (status IN ('OK', 'FAIL')),
    error_message    TEXT
);

CREATE INDEX idx_scheduler_run_job_finished ON scheduler_run(job_name, finished_at DESC);
