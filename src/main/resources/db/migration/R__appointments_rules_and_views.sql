CREATE OR REPLACE FUNCTION petify_trg_appointments_enforce()
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
        RAISE EXCEPTION 'Animal % not found for appointment', NEW.animal_id;
    END IF;

    IF NEW.responsible_owner_id <> v_animal_owner THEN
        RAISE EXCEPTION
            'Appointment responsible_owner_id (%) must match animals.owner_id (%) for animal %',
            NEW.responsible_owner_id, v_animal_owner, NEW.animal_id;
    END IF;

    IF NEW.status = 'CONFIRMED' AND NEW.date_time < now() THEN
        RAISE EXCEPTION
            'Cannot CONFIRM an appointment in the past (date_time=%)',
            NEW.date_time;
    END IF;

    IF NEW.status = 'DONE' AND NEW.date_time > now() THEN
        RAISE EXCEPTION
            'Cannot mark DONE for an appointment that is in the future (date_time=%)',
            NEW.date_time;
    END IF;

    RETURN NEW;
END;
$$;


-- Prevent overlapping 30-minute appointments for the same owner or animal
CREATE OR REPLACE FUNCTION petify_trg_appointments_no_overlap()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status NOT IN ('CONFIRMED', 'DONE') THEN
        RETURN NEW;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM appointments a
        WHERE a.responsible_owner_id = NEW.responsible_owner_id
          AND a.status IN ('CONFIRMED', 'DONE')
          AND tsrange(a.date_time, a.date_time + interval '30 minutes', '[)')
            && tsrange(NEW.date_time, NEW.date_time + interval '30 minutes', '[)')
          AND (TG_OP <> 'UPDATE' OR a.appointment_id <> NEW.appointment_id)
    ) THEN
        RAISE EXCEPTION
            'Overlapping appointment for owner % at %',
            NEW.responsible_owner_id, NEW.date_time;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM appointments a
        WHERE a.animal_id = NEW.animal_id
          AND a.status IN ('CONFIRMED', 'DONE')
          AND tsrange(a.date_time, a.date_time + interval '30 minutes', '[)')
            && tsrange(NEW.date_time, NEW.date_time + interval '30 minutes', '[)')
          AND (TG_OP <> 'UPDATE' OR a.appointment_id <> NEW.appointment_id)
    ) THEN
        RAISE EXCEPTION
            'Overlapping appointment for animal % at %',
            NEW.animal_id, NEW.date_time;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_appointments_enforce ON appointments;

CREATE TRIGGER trg_appointments_enforce
    BEFORE INSERT OR UPDATE
    ON appointments
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_appointments_enforce();


DROP TRIGGER IF EXISTS trg_appointments_no_overlap ON appointments;

CREATE TRIGGER trg_appointments_no_overlap
    BEFORE INSERT OR UPDATE
    ON appointments
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_appointments_no_overlap();

CREATE OR REPLACE VIEW v_clinic_appointments_monthly AS
SELECT
    clinic_id,
    date_trunc('month', date_time) AS month,
    COUNT(*) FILTER (WHERE status = 'CONFIRMED') AS confirmed_cnt,
    COUNT(*) FILTER (WHERE status = 'DONE')      AS done_cnt,
    COUNT(*) FILTER (WHERE status = 'NO_SHOW')   AS no_show_cnt,
    COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled_cnt,
    COUNT(*) AS total_cnt
FROM appointments
GROUP BY clinic_id, date_trunc('month', date_time)
ORDER BY month DESC;