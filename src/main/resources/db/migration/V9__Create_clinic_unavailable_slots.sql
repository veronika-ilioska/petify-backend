CREATE TABLE IF NOT EXISTS clinic_unavailable_slots (
    slot_id BIGSERIAL,
    clinic_id BIGINT NOT NULL,
    date_time TIMESTAMP NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT clinic_unavailable_slots_PK PRIMARY KEY (slot_id),
    CONSTRAINT clinic_unavailable_slots_clinic_FK FOREIGN KEY (clinic_id)
        REFERENCES vet_clinics(clinic_id)
        ON DELETE RESTRICT,
    CONSTRAINT clinic_unavailable_slots_UQ UNIQUE (clinic_id, date_time)
);
