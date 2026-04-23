
CREATE OR REPLACE FUNCTION petify_is_valid_listing_transition(p_old text, p_new text)
    RETURNS boolean
    LANGUAGE sql
AS $$
SELECT CASE
           WHEN p_old = p_new THEN true
           WHEN p_old = 'DRAFT'    AND p_new IN ('ACTIVE','ARCHIVED') THEN true
           WHEN p_old = 'ACTIVE'   AND p_new IN ('SOLD','ARCHIVED')   THEN true
           WHEN p_old = 'SOLD'     AND p_new IN ('ARCHIVED')          THEN true
           WHEN p_old = 'ARCHIVED' THEN false
           ELSE false
           END;
$$;

-- listings trigger function
CREATE OR REPLACE FUNCTION petify_trg_listings_enforce()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_animal_owner bigint;
BEGIN
    SELECT owner_id
    INTO v_animal_owner
    FROM animals
    WHERE animal_id = NEW.animal_id;

    IF v_animal_owner IS NULL THEN
        RAISE EXCEPTION 'Animal % not found', NEW.animal_id;
    END IF;

    IF NEW.owner_id <> v_animal_owner THEN
        RAISE EXCEPTION
            'Listing owner_id (%) must match animal.owner_id (%) for animal %',
            NEW.owner_id, v_animal_owner, NEW.animal_id;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        IF NOT petify_is_valid_listing_transition(OLD.status, NEW.status) THEN
            RAISE EXCEPTION 'Invalid listing status transition: % -> %', OLD.status, NEW.status;
        END IF;
    END IF;

    IF NEW.status = 'ACTIVE' THEN
        IF EXISTS (
            SELECT 1
            FROM listings l
            WHERE l.animal_id = NEW.animal_id
              AND l.status = 'ACTIVE'
              AND (TG_OP <> 'UPDATE' OR l.listing_id <> NEW.listing_id)
        ) THEN
            RAISE EXCEPTION 'Animal % already has an ACTIVE listing', NEW.animal_id;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_listings_enforce ON listings;



create trigger trg_listings_enforce
    before insert or update
    on listings
    for each row
execute function petify_trg_listings_enforce();

CREATE OR REPLACE VIEW v_listings_enriched AS
SELECT
    l.listing_id,
    l.status,
    l.price,
    l.created_at,
    l.owner_id AS listing_owner_id,
    a.animal_id,
    a.name AS animal_name,
    a.species,
    a.breed,
    a.located_name,
    a.owner_id AS animal_owner_id,
    (l.owner_id = a.owner_id) AS owner_match
FROM listings l
         JOIN animals a ON a.animal_id = l.animal_id;