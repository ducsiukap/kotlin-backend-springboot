CREATE TABLE comments
(
    id         BINARY(16) NOT NULL,
    content    TEXT     NOT NULL,
    user_id    BINARY(16) NOT NULL,
    post_id    BINARY(16) NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_comments PRIMARY KEY (id)
);

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

CREATE TABLE reactions
(
    id         BINARY(16) NOT NULL,
    user_id    BINARY(16) NOT NULL,
    post_id    BINARY(16) NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NULL,
    CONSTRAINT pk_reactions PRIMARY KEY (id)
);

CREATE TABLE users
(
    id           BINARY(16)   NOT NULL,
    first_name   VARCHAR(150) NOT NULL,
    last_name    VARCHAR(50)  NOT NULL,
    email        VARCHAR(50) NULL,
    day_of_birth date         NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    username     VARCHAR(50)  NOT NULL,
    password     VARCHAR(50)  NOT NULL,
    created_at   datetime     NOT NULL,
    updated_at   datetime NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);

ALTER TABLE comments
    ADD CONSTRAINT FK_COMMENTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE posts
    ADD CONSTRAINT FK_POSTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE reactions
    ADD CONSTRAINT FK_REACTIONS_ON_POST FOREIGN KEY (post_id) REFERENCES posts (id);

ALTER TABLE reactions
    ADD CONSTRAINT FK_REACTIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);