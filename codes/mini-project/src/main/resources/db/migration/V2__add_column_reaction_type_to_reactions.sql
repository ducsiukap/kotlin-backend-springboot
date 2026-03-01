ALTER TABLE reactions
    ADD reaction_type VARCHAR(20) NULL;

ALTER TABLE reactions
    MODIFY reaction_type VARCHAR (20) NOT NULL;