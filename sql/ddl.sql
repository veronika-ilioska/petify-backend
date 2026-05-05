BEGIN;

DROP TABLE IF EXISTS health_records CASCADE;
DROP TABLE IF EXISTS clinic_reviews CASCADE;
DROP TABLE IF EXISTS user_reviews CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS favorite_listings CASCADE;
DROP TABLE IF EXISTS listings CASCADE;
DROP TABLE IF EXISTS animals CASCADE;
DROP TABLE IF EXISTS vet_clinic_applications CASCADE;
DROP TABLE IF EXISTS vet_clinics CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS owners CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS users CASCADE;
drop table if exists clinic_unavailable_slots cascade ;

CREATE TABLE users (
                       user_id        BIGSERIAL,
                       username       VARCHAR(30)  NOT NULL,
                       email          VARCHAR(254) NOT NULL,
                       name           VARCHAR(60)  NOT NULL,
                       surname        VARCHAR(60)  NOT NULL,
                       password_hash  TEXT         NOT NULL,
                       created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
                       CONSTRAINT users_PK PRIMARY KEY (user_id),
                       CONSTRAINT users_username_UQ UNIQUE (username),
                       CONSTRAINT users_email_UQ    UNIQUE (email)
);

CREATE TABLE admins (
                        user_id BIGINT,
                        CONSTRAINT admins_PK PRIMARY KEY (user_id),
                        CONSTRAINT admins_user_FK FOREIGN KEY (user_id)
                            REFERENCES users(user_id)
                            ON DELETE RESTRICT
);

CREATE TABLE clients (
                         user_id        BIGINT,
                         is_blocked     BOOLEAN     NOT NULL DEFAULT FALSE,
                         blocked_at     TIMESTAMP,
                         blocked_reason TEXT,
                         blocked_by     BIGINT,
                         CONSTRAINT clients_PK PRIMARY KEY (user_id),
                         CONSTRAINT clients_user_FK FOREIGN KEY (user_id)
                             REFERENCES users(user_id)
                             ON DELETE RESTRICT,
                         CONSTRAINT clients_blocked_by_FK FOREIGN KEY (blocked_by)
                             REFERENCES admins(user_id)
                             ON DELETE SET NULL
);

CREATE TABLE owners (
                        user_id BIGINT,
                        CONSTRAINT owners_PK PRIMARY KEY (user_id),
                        CONSTRAINT owners_client_FK FOREIGN KEY (user_id)
                            REFERENCES clients(user_id)
                            ON DELETE RESTRICT
);

CREATE TABLE notifications (
                               notification_id BIGSERIAL,
                               user_id         BIGINT       NOT NULL,
                               type            VARCHAR(40)  NOT NULL,
                               message         TEXT         NOT NULL,
                               is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
                               created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
                               CONSTRAINT notifications_PK PRIMARY KEY (notification_id),
                               CONSTRAINT notifications_user_FK FOREIGN KEY (user_id)
                                   REFERENCES users(user_id)
                                   ON DELETE RESTRICT
);

CREATE TABLE vet_clinic_applications (
                                         application_id BIGSERIAL,
                                         name           VARCHAR(120) NOT NULL,
                                         email          VARCHAR(254),
                                         phone          VARCHAR(40),
                                         city           VARCHAR(80)  NOT NULL,
                                         address        VARCHAR(200) NOT NULL,
                                         submitted_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
                                         status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
                                         reviewed_at    TIMESTAMP,
                                         reviewed_by    BIGINT,
                                         denial_reason  TEXT,

                                         CONSTRAINT vet_clinic_applications_PK PRIMARY KEY (application_id),

                                         CONSTRAINT vet_clinic_applications_admin_FK FOREIGN KEY (reviewed_by)
                                             REFERENCES admins(user_id)
                                             ON DELETE SET NULL,

                                         CONSTRAINT vet_clinic_applications_status_chk CHECK (
                                             status IN ('PENDING','APPROVED','DENIED')
                                             )
);

CREATE TABLE vet_clinics (
                             clinic_id BIGSERIAL,
                             name      VARCHAR(120) NOT NULL,
                             email     VARCHAR(254),
                             phone     VARCHAR(40),
                             location  VARCHAR(120),
                             city      VARCHAR(80)  NOT NULL,
                             address   VARCHAR(200) NOT NULL,
                             user_id   BIGINT       UNIQUE,
                             application_id BIGINT UNIQUE,
                             CONSTRAINT vet_clinics_PK PRIMARY KEY (clinic_id),
                             CONSTRAINT vet_clinics_user_FK FOREIGN KEY (user_id)
                                 REFERENCES users(user_id)
                                 ON DELETE RESTRICT,
                             CONSTRAINT vet_clinics_application_fk FOREIGN KEY (application_id)
                                 REFERENCES vet_clinic_applications(application_id)
                                 ON DELETE RESTRICT
);



CREATE TABLE animals (
                         animal_id     BIGSERIAL,
                         owner_id      BIGINT      NOT NULL,
                         name          VARCHAR(80) NOT NULL,
                         sex           VARCHAR(10) NOT NULL,
                         date_of_birth DATE,
                         photo_url     TEXT,
                         type          VARCHAR(40),
                         species       VARCHAR(60),
                         breed         VARCHAR(60),
                         located_name  VARCHAR(120),
                         CONSTRAINT animals_PK PRIMARY KEY (animal_id),
                         CONSTRAINT animals_owner_FK FOREIGN KEY (owner_id)
                             REFERENCES owners(user_id)
                             ON DELETE RESTRICT,
                         CONSTRAINT animals_sex_CHK CHECK (sex IN ('MALE','FEMALE','UNKNOWN'))
);

CREATE TABLE listings (
                          listing_id   BIGSERIAL,
                          owner_id     BIGINT        NOT NULL,
                          animal_id    BIGINT        NOT NULL,
                          status       VARCHAR(20)   NOT NULL,
                          price        NUMERIC(12,2) NOT NULL,
                          description  TEXT,
                          created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),

                          CONSTRAINT listings_PK PRIMARY KEY (listing_id),
                          CONSTRAINT listings_owner_FK FOREIGN KEY (owner_id)
                              REFERENCES owners(user_id)
                              ON DELETE RESTRICT,
                          CONSTRAINT listings_animal_FK FOREIGN KEY (animal_id)
                              REFERENCES animals(animal_id)
                              ON DELETE RESTRICT,
                          CONSTRAINT listings_status_CHK CHECK (status IN ('DRAFT','ACTIVE','SOLD','ARCHIVED')),
                          CONSTRAINT listings_price_CHK CHECK (price >= 0)
);

CREATE TABLE favorite_listings (
                                   client_id  BIGINT NOT NULL,
                                   listing_id BIGINT NOT NULL,
                                   CONSTRAINT favorite_listings_PK PRIMARY KEY (client_id, listing_id),
                                   CONSTRAINT favorite_listings_client_FK FOREIGN KEY (client_id)
                                       REFERENCES clients(user_id)
                                       ON DELETE CASCADE,
                                   CONSTRAINT favorite_listings_listing_FK FOREIGN KEY (listing_id)
                                       REFERENCES listings(listing_id)
                                       ON DELETE CASCADE
);

CREATE TABLE appointments (
                              appointment_id       BIGSERIAL,
                              clinic_id            BIGINT      NOT NULL,
                              animal_id            BIGINT      NOT NULL,
                              responsible_owner_id BIGINT      NOT NULL,
                              status               VARCHAR(20) NOT NULL,
                              date_time            TIMESTAMP   NOT NULL,
                              notes                TEXT,

                              CONSTRAINT appointments_PK PRIMARY KEY (appointment_id),
                              CONSTRAINT appointments_clinic_FK FOREIGN KEY (clinic_id)
                                  REFERENCES vet_clinics(clinic_id)
                                  ON DELETE RESTRICT,
                              CONSTRAINT appointments_animal_FK FOREIGN KEY (animal_id)
                                  REFERENCES animals(animal_id)
                                  ON DELETE RESTRICT,
                              CONSTRAINT appointments_owner_FK FOREIGN KEY (responsible_owner_id)
                                  REFERENCES owners(user_id)
                                  ON DELETE RESTRICT,
                              CONSTRAINT appointments_status_CHK CHECK (status IN ('CONFIRMED','CANCELLED','DONE','NO_SHOW'))
);

CREATE TABLE health_records (
                                healthrecord_id BIGSERIAL,
                                animal_id       BIGINT      NOT NULL,
                                appointment_id  BIGINT      NOT NULL,
                                type            VARCHAR(40) NOT NULL,
                                description     TEXT,
                                date            DATE        NOT NULL,

                                CONSTRAINT health_records_PK PRIMARY KEY (healthrecord_id),
                                CONSTRAINT health_records_animal_FK FOREIGN KEY (animal_id)
                                    REFERENCES animals(animal_id)
                                    ON DELETE RESTRICT,
                                CONSTRAINT health_records_appointment_FK FOREIGN KEY (appointment_id)
                                    REFERENCES appointments(appointment_id)
                                    ON DELETE RESTRICT
);

CREATE TABLE reviews (
                         review_id    BIGSERIAL,
                         reviewer_id  BIGINT     NOT NULL,
                         rating       rating_1_5     NOT NULL,
                         comment      TEXT,
                         created_at   TIMESTAMP  NOT NULL DEFAULT NOW(),
                         updated_at   TIMESTAMP,
                         is_deleted   BOOLEAN    NOT NULL DEFAULT FALSE,
                         CONSTRAINT reviews_PK PRIMARY KEY (review_id),
                         CONSTRAINT reviews_reviewer_FK FOREIGN KEY (reviewer_id)
                             REFERENCES clients(user_id)
                             ON DELETE RESTRICT,
                         CONSTRAINT reviews_rating_CHK CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE user_reviews (
                              review_id      BIGINT,
                              target_user_id BIGINT NOT NULL,

                              CONSTRAINT user_reviews_PK PRIMARY KEY (review_id),
                              CONSTRAINT user_reviews_review_FK FOREIGN KEY (review_id)
                                  REFERENCES reviews(review_id)
                                  ON DELETE RESTRICT,
                              CONSTRAINT user_reviews_target_FK FOREIGN KEY (target_user_id)
                                  REFERENCES users(user_id)
                                  ON DELETE RESTRICT
);

CREATE TABLE clinic_reviews (
                                review_id        BIGINT,
                                target_clinic_id BIGINT NOT NULL,

                                CONSTRAINT clinic_reviews_PK PRIMARY KEY (review_id),
                                CONSTRAINT clinic_reviews_review_FK FOREIGN KEY (review_id)
                                    REFERENCES reviews(review_id)
                                    ON DELETE RESTRICT,
                                CONSTRAINT clinic_reviews_target_FK FOREIGN KEY (target_clinic_id)
                                    REFERENCES vet_clinics(clinic_id)
                                    ON DELETE RESTRICT
);

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

COMMIT ;