ALTER TABLE reservation ADD COLUMN notified_at TIMESTAMPTZ;
CREATE INDEX idx_reservation_notified_at ON reservation(notified_at);
