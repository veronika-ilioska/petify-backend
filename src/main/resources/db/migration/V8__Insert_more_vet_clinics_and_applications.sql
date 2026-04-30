BEGIN;

INSERT INTO vet_clinic_applications
    (name, email, phone, city, address, submitted_at, status, reviewed_at, reviewed_by, denial_reason)
VALUES
    ('PetWell Clinic', 'hello@petwell.vet', '+389 70 555 666', 'Ohrid', 'Kej Makedonija 12',
     NOW() - INTERVAL '35 days', 'APPROVED', NOW() - INTERVAL '34 days',
     (SELECT user_id FROM users WHERE username='admin.ana'), NULL),
    ('CityVet Hub', 'contact@cityvet.vet', '+389 70 777 888', 'Skopje', 'Bulevar 3',
     NOW() - INTERVAL '33 days', 'APPROVED', NOW() - INTERVAL '32 days',
     (SELECT user_id FROM users WHERE username='admin.ana'), NULL),
    ('Paw & Care', 'info@pawandcare.vet', '+389 70 999 000', 'Bitola', 'Marshal Tito 22',
     NOW() - INTERVAL '31 days', 'APPROVED', NOW() - INTERVAL '30 days',
     (SELECT user_id FROM users WHERE username='admin.ana'), NULL),
    ('North Vet Clinic', 'hello@northvet.vet', '+389 70 222 333', 'Kumanovo', 'Goce Delcev 8',
     NOW() - INTERVAL '29 days', 'APPROVED', NOW() - INTERVAL '28 days',
     (SELECT user_id FROM users WHERE username='admin.ana'), NULL),
    ('Sunny Paws Vet', 'contact@sunnyvet.vet', '+389 70 444 555', 'Stip', 'Marshal Tito 4',
     NOW() - INTERVAL '27 days', 'APPROVED', NOW() - INTERVAL '26 days',
     (SELECT user_id FROM users WHERE username='admin.ana'), NULL);

INSERT INTO vet_clinics (name, email, phone, location, city, address, application_id)
VALUES
    ('PetWell Clinic', 'hello@petwell.vet', '+389 70 555 666', 'Lake', 'Ohrid', 'Kej Makedonija 12',
     (SELECT application_id FROM vet_clinic_applications WHERE name='PetWell Clinic' ORDER BY application_id DESC LIMIT 1)),
    ('CityVet Hub', 'contact@cityvet.vet', '+389 70 777 888', 'Center', 'Skopje', 'Bulevar 3',
     (SELECT application_id FROM vet_clinic_applications WHERE name='CityVet Hub' ORDER BY application_id DESC LIMIT 1)),
    ('Paw & Care', 'info@pawandcare.vet', '+389 70 999 000', 'Old Town', 'Bitola', 'Marshal Tito 22',
     (SELECT application_id FROM vet_clinic_applications WHERE name='Paw & Care' ORDER BY application_id DESC LIMIT 1)),
    ('North Vet Clinic', 'hello@northvet.vet', '+389 70 222 333', 'Center', 'Kumanovo', 'Goce Delcev 8',
     (SELECT application_id FROM vet_clinic_applications WHERE name='North Vet Clinic' ORDER BY application_id DESC LIMIT 1)),
    ('Sunny Paws Vet', 'contact@sunnyvet.vet', '+389 70 444 555', 'Center', 'Stip', 'Marshal Tito 4',
     (SELECT application_id FROM vet_clinic_applications WHERE name='Sunny Paws Vet' ORDER BY application_id DESC LIMIT 1));

COMMIT;

