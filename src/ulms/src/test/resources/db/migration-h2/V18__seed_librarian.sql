MERGE INTO users (name, email, role, password_hash, active, failed_login_attempts)
KEY (email)
VALUES (
    'Default Librarian',
    'librarian@ulms.local',
    'LIBRARIAN',
    '$2a$10$b/Tfn2b5wBurjq1hlc9Fh.UNTK.dTFHbQjccnjlXuFfvx4IzPXd2m',
    TRUE,
    0
);
