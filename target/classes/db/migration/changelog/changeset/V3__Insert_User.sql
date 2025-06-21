INSERT INTO users (username, password, status)
VALUES ('alex', '$2a$12$U4bifA9ga/X8SebLlilneO/HRuPRwDMHtMsPONN3xoHL0zeg/a61a', 'ACTIVE'),
       ('boby', '$2a$12$U4bifA9ga/X8SebLlilneO/HRuPRwDMHtMsPONN3xoHL0zeg/a61a', 'ACTIVE'),
       ('bob','$2a$12$U4bifA9ga/X8SebLlilneO/HRuPRwDMHtMsPONN3xoHL0zeg/a61a','ACTIVE');

INSERT INTO users_roles (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3,2);

INSERT INTO cards (card_number, expiry_date, balance, status, owner_id)
VALUES (3043885493415683, '2025-12-31 23:59:59', 200.00, 'ACTIVE', 1);


