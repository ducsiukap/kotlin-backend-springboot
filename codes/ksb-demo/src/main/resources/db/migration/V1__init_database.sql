CREATE TABLE posts
(
    id         BINARY(16)   NOT NULL,
    title      VARCHAR(255) NOT NULL,
    content    TEXT NULL,
    user_id    BINARY(16)   NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id)
);

CREATE TABLE users
(
    id         BINARY(16)   NOT NULL,
    full_name  VARCHAR(150) NOT NULL,
    email      VARCHAR(20)  NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);