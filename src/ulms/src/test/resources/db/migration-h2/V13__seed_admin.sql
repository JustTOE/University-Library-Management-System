MERGE INTO users (name, email, role, password_hash, active, failed_login_attempts)
KEY (email)
VALUES (
    'Default Admin',
    'admin@ulms.local',
    'ADMIN',
    '$2a$10$kDHxvn/UZmKbfKMusDCvcu78bEftor70gICk24Zcd0L00NOhKOp2.',
    TRUE,
    0
);
