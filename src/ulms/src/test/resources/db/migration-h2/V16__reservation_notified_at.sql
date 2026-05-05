ALTER TABLE reservation ADD COLUMN notified_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_reservation_notified_at ON reservation(notified_at);
