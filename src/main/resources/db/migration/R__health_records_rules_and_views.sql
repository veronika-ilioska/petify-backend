CREATE OR REPLACE FUNCTION petify_trg_health_records_enforce()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_appt_animal bigint;
    v_appt_status text;
    v_appt_date date;
BEGIN
    SELECT a.animal_id, a.status, a.date_time::date
    INTO v_appt_animal, v_appt_status, v_appt_date
    FROM appointments a
    WHERE a.appointment_id = NEW.appointment_id;

    IF v_appt_animal IS NULL THEN
        RAISE EXCEPTION 'Appointment % not found for health record', NEW.appointment_id;
    END IF;

    IF NEW.animal_id <> v_appt_animal THEN
        RAISE EXCEPTION
            'Health record animal_id (%) must match appointment animal_id (%) for appointment %',
            NEW.animal_id, v_appt_animal, NEW.appointment_id;
    END IF;

    IF v_appt_status <> 'DONE' THEN
        RAISE EXCEPTION
            'Cannot insert health record unless appointment % is DONE (current status=%)',
            NEW.appointment_id, v_appt_status;
    END IF;

    IF NEW.date <> v_appt_date THEN
        RAISE EXCEPTION
            'Health record date (%) must equal appointment date (%) for appointment %',
            NEW.date, v_appt_date, NEW.appointment_id;
    END IF;

    RETURN NEW;
END;
$$;


DROP TRIGGER IF EXISTS trg_health_records_enforce ON health_records;

CREATE TRIGGER trg_health_records_enforce
    BEFORE INSERT OR UPDATE
    ON health_records
    FOR EACH ROW
EXECUTE FUNCTION petify_trg_health_records_enforce();


CREATE OR REPLACE VIEW v_health_records_with_context AS
SELECT
    hr.healthrecord_id,
    hr.animal_id,
    a.name AS animal_name,
    hr.appointment_id,
    ap.clinic_id,
    vc.name AS clinic_name,
    ap.date_time,
    ap.status AS appointment_status,
    hr.type,
    hr.description,
    hr.date
FROM health_records hr
         JOIN animals a       ON a.animal_id = hr.animal_id
         JOIN appointments ap ON ap.appointment_id = hr.appointment_id
         LEFT JOIN vet_clinics vc ON vc.clinic_id = ap.clinic_id;
