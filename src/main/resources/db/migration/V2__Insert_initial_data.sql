BEGIN;

INSERT INTO users (username, email, name, surname, password_hash, created_at) VALUES
                                                                                  ('client.viktor', 'viktor.client@petify.com', 'Viktor', 'Kostov',   '$2a$10$c0bwBEKf6v5yFO1jGOcXyuyB8hh7b7NUZeRqp4Y8zR7L6x9P5oO5i', NOW() - INTERVAL '42 days'),
                                                                                  ('admin.ana',     'ana.admin@petify.com',     'Ana',    'Adminova', '$2a$10$c0bwBEKf6v5yFO1jGOcXyuyB8hh7b7NUZeRqp4Y8zR7L6x9P5oO5i', NOW() - INTERVAL '40 days'),
                                                                                  ('client.mila',   'mila.client@petify.com',   'Mila',   'Ivanova',  '$2a$10$c0bwBEKf6v5yFO1jGOcXyuyB8hh7b7NUZeRqp4Y8zR7L6x9P5oO5i', NOW() - INTERVAL '30 days'),
                                                                                  ('client.igor',   'igor.client@petify.com',   'Igor',   'Petrov',   '$2a$10$c0bwBEKf6v5yFO1jGOcXyuyB8hh7b7NUZeRqp4Y8zR7L6x9P5oO5i', NOW() - INTERVAL '25 days'),
                                                                                  ('client.sara',   'sara.client@petify.com',   'Sara',   'Jovanova', '$2a$10$c0bwBEKf6v5yFO1jGOcXyuyB8hh7b7NUZeRqp4Y8zR7L6x9P5oO5i', NOW() - INTERVAL '20 days'),
                                                                                  ('clinic.happypaws', 'clinic.happypaws@petify.com', 'Happy Paws', 'Clinic',
                                                                                   '$2a$10$JE4p.bHmOPHFTuJYIOFf4uN8lRb3FH7RjZY.CGXp9Ui69ptgYwksO', NOW()),
                                                                                  ('clinic.vetcare', 'clinic.vetcare@petify.com', 'VetCare', 'Center',
                                                                                   '$2a$10$JE4p.bHmOPHFTuJYIOFf4uN8lRb3FH7RjZY.CGXp9Ui69ptgYwksO', NOW());

INSERT INTO admins (user_id)
SELECT user_id FROM users WHERE username = 'admin.ana';

INSERT INTO clients (user_id)
SELECT user_id
FROM users
WHERE username IN ('client.viktor','client.mila','client.igor','client.sara');

INSERT INTO owners (user_id)
SELECT user_id FROM users WHERE username IN ('client.mila','client.igor');
INSERT INTO vet_clinic_applications
(name, email, phone, city, address, submitted_at, status, reviewed_at, reviewed_by, denial_reason)
VALUES
    (
        'Happy Paws Clinic',
        'contact@happypaws.vet',
        '+389 70 111 222',
        'Skopje',
        'Partizanska 10',
        NOW() - INTERVAL '41 days',
        'APPROVED',
        NOW() - INTERVAL '40 days',
        (SELECT user_id FROM users WHERE username = 'admin.ana'),
        NULL
    ),
    (
        'VetCare Center',
        'info@vetcare.vet',
        '+389 70 333 444',
        'Bitola',
        'Shirok Sokak 55',
        NOW() - INTERVAL '39 days',
        'APPROVED',
        NOW() - INTERVAL '38 days',
        (SELECT user_id FROM users WHERE username = 'admin.ana'),
        NULL
    );

INSERT INTO vet_clinics
(name, email, phone, location, city, address, user_id, application_id)
VALUES
    (
        'Happy Paws Clinic',
        'contact@happypaws.vet',
        '+389 70 111 222',
        'Center',
        'Skopje',
        'Partizanska 10',
        (SELECT user_id FROM users WHERE username = 'clinic.happypaws'),
        (SELECT application_id
         FROM vet_clinic_applications
         WHERE name = 'Happy Paws Clinic')
    ),
    (
        'VetCare Center',
        'info@vetcare.vet',
        '+389 70 333 444',
        'Downtown',
        'Bitola',
        'Shirok Sokak 55',
        (SELECT user_id FROM users WHERE username = 'clinic.vetcare'),
        (SELECT application_id
         FROM vet_clinic_applications
         WHERE name = 'VetCare Center')
    );

INSERT INTO animals (owner_id, name, sex, date_of_birth, photo_url, species, breed, located_name) VALUES
                                                                                                            ((SELECT user_id FROM users WHERE username='client.mila'), 'Luna', 'FEMALE', '2021-05-10', NULL, 'Dog', 'Labrador', 'Skopje'),
                                                                                                            ((SELECT user_id FROM users WHERE username='client.mila'), 'Max',  'MALE',   '2020-11-02', NULL, 'Cat', 'British Shorthair', 'Skopje'),
                                                                                                            ((SELECT user_id FROM users WHERE username='client.igor'), 'Rex',  'MALE',   '2019-02-18', NULL, 'Dog', 'German Shepherd', 'Bitola');

INSERT INTO listings (owner_id, animal_id, status, price, description, created_at) VALUES
                                                                                       ((SELECT user_id FROM users WHERE username='client.mila'),
                                                                                        (SELECT animal_id FROM animals WHERE name='Luna'),
                                                                                        'ACTIVE', 50.00, 'Friendly dog available for weekend sitting.', NOW() - INTERVAL '10 days'),
                                                                                       ((SELECT user_id FROM users WHERE username='client.igor'),
                                                                                        (SELECT animal_id FROM animals WHERE name='Rex'),
                                                                                        'ARCHIVED', 35.00, 'Guard dog training sessions (experienced handler).', NOW() - INTERVAL '7 days');

INSERT INTO appointments (clinic_id, animal_id, responsible_owner_id, status, date_time, notes) VALUES
                                                                                                    ((SELECT clinic_id FROM vet_clinics WHERE name='Happy Paws Clinic'),
                                                                                                     (SELECT animal_id FROM animals WHERE name='Luna'),
                                                                                                     (SELECT user_id FROM users WHERE username='client.mila'),
                                                                                                     'DONE', NOW() - INTERVAL '5 days', 'Vaccination'),
                                                                                                    ((SELECT clinic_id FROM vet_clinics WHERE name='Happy Paws Clinic'),
                                                                                                     (SELECT animal_id FROM animals WHERE name='Max'),
                                                                                                     (SELECT user_id FROM users WHERE username='client.mila'),
                                                                                                     'CONFIRMED', NOW() + INTERVAL '2 days', 'Check-up'),
                                                                                                    ((SELECT clinic_id FROM vet_clinics WHERE name='VetCare Center'),
                                                                                                     (SELECT animal_id FROM animals WHERE name='Rex'),
                                                                                                     (SELECT user_id FROM users WHERE username='client.igor'),
                                                                                                     'DONE', NOW() - INTERVAL '12 days', 'Minor injury treatment');

INSERT INTO reviews (review_id, reviewer_id, rating, comment, created_at) VALUES
                                                                              (1, (SELECT user_id FROM users WHERE username='client.mila'),
                                                                               5, 'Great service and friendly staff!', NOW() - INTERVAL '4 days'),
                                                                              (2, (SELECT user_id FROM users WHERE username='client.sara'),
                                                                               4, 'Smooth communication and on time.', NOW() - INTERVAL '3 days'),
                                                                              (3, (SELECT user_id FROM users WHERE username='client.viktor'),
                                                                               4, 'Very reliable and polite communication.', NOW() - INTERVAL '2 days');

INSERT INTO clinic_reviews (review_id, target_clinic_id)
VALUES (1, (SELECT clinic_id FROM vet_clinics WHERE name='Happy Paws Clinic'));

INSERT INTO user_reviews (review_id, target_user_id) VALUES
                                                         (2, (SELECT user_id FROM users WHERE username='client.igor')),
                                                         (3, (SELECT user_id FROM users WHERE username='client.mila'));

INSERT INTO health_records (animal_id, appointment_id, type, description, date) VALUES
                                                                                    ((SELECT animal_id FROM animals WHERE name='Luna'),
                                                                                     (SELECT appointment_id FROM appointments a
                                                                                                                     JOIN animals an ON an.animal_id = a.animal_id
                                                                                      WHERE an.name='Luna' AND a.status='DONE'
                                                                                      ORDER BY a.appointment_id DESC LIMIT 1),
    'VACCINATION', 'Rabies vaccine administered. No adverse reaction.', CURRENT_DATE - 5),
((SELECT animal_id FROM animals WHERE name='Rex'),
 (SELECT appointment_id FROM appointments a
   JOIN animals an ON an.animal_id = a.animal_id
   WHERE an.name='Rex' AND a.status='DONE'
   ORDER BY a.appointment_id DESC LIMIT 1),
 'TREATMENT', 'Wound cleaned and bandaged. Antibiotics prescribed.', CURRENT_DATE - 12);

INSERT INTO notifications (user_id, type, message, is_read, created_at) VALUES
                                                                            ((SELECT user_id FROM users WHERE username='client.mila'), 'APPOINTMENT', 'Your appointment is confirmed for Max.', FALSE, NOW() - INTERVAL '1 day'),
                                                                            ((SELECT user_id FROM users WHERE username='client.igor'), 'LISTING',     'Your listing status is ARCHIVED.',       TRUE,  NOW() - INTERVAL '6 days');

INSERT INTO clinic_unavailable_slots (clinic_id, date_time, reason, created_at)
VALUES
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'Happy Paws Clinic'),
        NOW() + INTERVAL '1 day',
        'Doctor unavailable - private appointment',
        NOW()
    ),
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'Happy Paws Clinic'),
        NOW() + INTERVAL '3 days',
        'Clinic equipment maintenance',
        NOW()
    ),
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'Happy Paws Clinic'),
        NOW() + INTERVAL '7 days',
        'Staff training session',
        NOW()
    ),
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'VetCare Center'),
        NOW() + INTERVAL '2 days',
        'Emergency-only working hours',
        NOW()
    ),
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'VetCare Center'),
        NOW() + INTERVAL '5 days',
        'Veterinarian on leave',
        NOW()
    ),
    (
        (SELECT clinic_id FROM vet_clinics WHERE name = 'VetCare Center'),
        NOW() + INTERVAL '10 days',
        'Clinic closed for local holiday',
        NOW()
    );

COMMIT;
