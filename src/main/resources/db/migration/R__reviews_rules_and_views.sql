create or replace function petify_trg_reviews_no_update()
returns trigger
language plpgsql
as $$
begin
    raise exception 'Reviews cannot be updated';
end;
$$;

DROP TRIGGER IF EXISTS trg_reviews_no_update ON reviews;

create trigger trg_reviews_no_update
before update on reviews
for each row
execute function petify_trg_reviews_no_update();


DROP TRIGGER IF EXISTS trg_clinic_reviews_no_update ON clinic_reviews;
CREATE TRIGGER trg_clinic_reviews_no_update
    BEFORE UPDATE
    ON clinic_reviews
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_reviews_no_update();


-- A review_id can belong to either user_reviews or clinic_reviews, but not both
CREATE OR REPLACE FUNCTION petify_trg_user_review_exclusive()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM clinic_reviews cr
        WHERE cr.review_id = NEW.review_id
    ) THEN
        RAISE EXCEPTION
            'review_id % already used as clinic review (cannot also be user review)',
            NEW.review_id;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_user_review_exclusive ON user_reviews;
CREATE TRIGGER trg_user_review_exclusive
    BEFORE INSERT
    ON user_reviews
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_user_review_exclusive();


CREATE OR REPLACE FUNCTION petify_trg_clinic_review_exclusive()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM user_reviews ur
        WHERE ur.review_id = NEW.review_id
    ) THEN
        RAISE EXCEPTION
            'review_id % already used as user review (cannot also be clinic review)',
            NEW.review_id;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_clinic_review_exclusive ON clinic_reviews;
CREATE TRIGGER trg_clinic_review_exclusive
    BEFORE INSERT
    ON clinic_reviews
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_clinic_review_exclusive();


-- Cooldown
CREATE OR REPLACE FUNCTION petify_trg_user_reviews_cooldown()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_reviewer bigint;
    v_created  timestamp;
BEGIN
    SELECT reviewer_id, created_at
    INTO v_reviewer, v_created
    FROM reviews
    WHERE review_id = NEW.review_id;

    IF v_reviewer IS NULL THEN
        RAISE EXCEPTION 'Base review % not found', NEW.review_id;
    END IF;

    IF v_reviewer = NEW.target_user_id THEN
        RAISE EXCEPTION 'User cannot review themselves (user_id=%)', v_reviewer;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM user_reviews ur
                 JOIN reviews r ON r.review_id = ur.review_id
        WHERE r.reviewer_id = v_reviewer
          AND ur.target_user_id = NEW.target_user_id
          AND r.is_deleted = false
          AND r.created_at >= v_created - interval '30 days'
          AND r.review_id <> NEW.review_id
    ) THEN
        RAISE EXCEPTION
            'Cooldown: reviewer % already reviewed user % within last 30 days',
            v_reviewer, NEW.target_user_id;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_user_reviews_cooldown ON user_reviews;
CREATE TRIGGER trg_user_reviews_cooldown
    BEFORE INSERT
    ON user_reviews
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_user_reviews_cooldown();


-- Cooldown
CREATE OR REPLACE FUNCTION petify_trg_clinic_reviews_cooldown()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_reviewer bigint;
    v_created  timestamp;
BEGIN
    SELECT reviewer_id, created_at
    INTO v_reviewer, v_created
    FROM reviews
    WHERE review_id = NEW.review_id;

    IF v_reviewer IS NULL THEN
        RAISE EXCEPTION 'Base review % not found', NEW.review_id;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM clinic_reviews cr
                 JOIN reviews r ON r.review_id = cr.review_id
        WHERE r.reviewer_id = v_reviewer
          AND cr.target_clinic_id = NEW.target_clinic_id
          AND r.is_deleted = false
          AND r.created_at >= v_created - interval '30 days'
          AND r.review_id <> NEW.review_id
    ) THEN
        RAISE EXCEPTION
            'Cooldown: reviewer % already reviewed clinic % within last 30 days',
            v_reviewer, NEW.target_clinic_id;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_clinic_reviews_cooldown ON clinic_reviews;
CREATE TRIGGER trg_clinic_reviews_cooldown
    BEFORE INSERT
    ON clinic_reviews
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_clinic_reviews_cooldown();


CREATE OR REPLACE VIEW v_user_ratings AS
SELECT
    ur.target_user_id,
    COUNT(*) FILTER (WHERE r.is_deleted = false) AS review_count,
    ROUND(AVG(r.rating) FILTER (WHERE r.is_deleted = false), 2) AS avg_rating
FROM user_reviews ur
         JOIN reviews r ON r.review_id = ur.review_id
GROUP BY ur.target_user_id;


CREATE OR REPLACE VIEW v_clinic_ratings AS
SELECT
    cr.target_clinic_id,
    COUNT(*) FILTER (WHERE r.is_deleted = false) AS review_count,
    ROUND(AVG(r.rating) FILTER (WHERE r.is_deleted = false), 2) AS avg_rating
FROM clinic_reviews cr
         JOIN reviews r ON r.review_id = cr.review_id
GROUP BY cr.target_clinic_id;