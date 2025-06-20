-- Таблица пользователей
CREATE TABLE users
(
    id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(64)  NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    status   VARCHAR(20)
);

-- Таблица ролей
CREATE TABLE roles
(
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(64) NOT NULL
);

-- Таблица связи many-to-many users_roles
CREATE TABLE users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Таблица карт
CREATE TABLE cards
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    card_number VARCHAR(16)    NOT NULL UNIQUE,
    expiry_date TIMESTAMP      NOT NULL,
    balance     DECIMAL(19, 2) NOT NULL,
    status      VARCHAR(20),
    owner_id    BIGINT         NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cards_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);




