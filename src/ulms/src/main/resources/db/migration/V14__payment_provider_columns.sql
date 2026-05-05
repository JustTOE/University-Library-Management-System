ALTER TABLE payment ADD COLUMN provider_ref VARCHAR(64);
ALTER TABLE payment ADD COLUMN decline_reason VARCHAR(200);
