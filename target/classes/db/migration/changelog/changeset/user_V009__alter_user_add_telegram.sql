ALTER TABLE users
ADD COLUMN if not exists telegram_login varchar(255),
ADD COLUMN if not exists telegram_chat_id bigint;
