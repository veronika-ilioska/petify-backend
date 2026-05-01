ALTER TABLE vet_clinics
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

INSERT INTO users (username, email, name, surname, password_hash, created_at)
SELECT 'clinic.happypaws', 'clinic.happypaws@petify.com', 'Happy Paws', 'Clinic',
       '$2a$10$JE4p.bHmOPHFTuJYIOFf4uN8lRb3FH7RjZY.CGXp9Ui69ptgYwksO', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'clinic.happypaws');

INSERT INTO users (username, email, name, surname, password_hash, created_at)
SELECT 'clinic.vetcare', 'clinic.vetcare@petify.com', 'VetCare', 'Center',
       '$2a$10$JE4p.bHmOPHFTuJYIOFf4uN8lRb3FH7RjZY.CGXp9Ui69ptgYwksO', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'clinic.vetcare');

UPDATE vet_clinics
SET user_id = (SELECT user_id FROM users WHERE username = 'clinic.happypaws')
WHERE name = 'Happy Paws Clinic'
  AND user_id IS NULL;

UPDATE vet_clinics
SET user_id = (SELECT user_id FROM users WHERE username = 'clinic.vetcare')
WHERE name = 'VetCare Center'
  AND user_id IS NULL;

ALTER TABLE vet_clinics
    DROP CONSTRAINT IF EXISTS vet_clinics_user_FK;

ALTER TABLE vet_clinics
    ADD CONSTRAINT vet_clinics_user_FK FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE RESTRICT;

ALTER TABLE vet_clinics
    DROP CONSTRAINT IF EXISTS vet_clinics_user_UQ;

ALTER TABLE vet_clinics
    ADD CONSTRAINT vet_clinics_user_UQ UNIQUE (user_id);
