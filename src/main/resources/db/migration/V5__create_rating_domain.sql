CREATE DOMAIN rating_1_5 AS int
    CHECK (VALUE BETWEEN 1 AND 5);

DROP VIEW IF EXISTS v_user_ratings;
DROP VIEW IF EXISTS v_clinic_ratings;

ALTER TABLE reviews
    ALTER COLUMN rating TYPE rating_1_5;