INSERT INTO users (username,password,status)
VALUES ('alex',1234,'ACTIVE'),
       ('boby',4321,'ACTIVE');

INSERT INTO users_roles (user_id,role_id)
VALUES (1,1),
       (2,2);

INSERT INTO cards (card_number,expiry_date,balance,status,owner_id)
VALUES (3043885493415683, '2025-12-31 23:59:59',200.00,'ACTIVE',1);


