BEGIN;

ALTER TABLE vet_clinics
    ADD COLUMN application_id BIGINT;

UPDATE vet_clinics vc
SET application_id = vca.application_id
FROM vet_clinic_applications vca
WHERE vca.clinic_id = vc.clinic_id;

ALTER TABLE vet_clinics
    ALTER COLUMN application_id SET NOT NULL;

ALTER TABLE vet_clinics
    ADD CONSTRAINT vet_clinics_application_uq UNIQUE (application_id);

ALTER TABLE vet_clinics
    ADD CONSTRAINT vet_clinics_application_fk FOREIGN KEY (application_id)
        REFERENCES vet_clinic_applications(application_id)
        ON DELETE RESTRICT;

ALTER TABLE vet_clinic_applications
    DROP CONSTRAINT IF EXISTS vet_clinic_applications_clinic_FK;

ALTER TABLE vet_clinic_applications
    DROP COLUMN clinic_id;

COMMIT;

