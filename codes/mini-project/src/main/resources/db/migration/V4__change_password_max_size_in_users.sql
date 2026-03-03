-- BcryptEncoder -> ~60 characters
ALTER TABLE users
    MODIFY password VARCHAR(100);