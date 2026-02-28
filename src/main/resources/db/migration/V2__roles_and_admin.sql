INSERT INTO roles (name) VALUES ('USER') ON CONFLICT DO NOTHING;
INSERT INTO roles (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO users (username, password_hash, enabled)
VALUES ('admin', '$2a$10$0HqYfwGKSi1NQNT2bm6I4u55g/UHKurgj6ZuPjNZ88OeoHNCO..mG', TRUE)
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.username = 'admin'
    ON CONFLICT DO NOTHING;