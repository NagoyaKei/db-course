-- sql/01_init.sql

-- =============================================
-- PERSON
-- =============================================
CREATE TABLE person (
                        person_id       INT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        last_name       VARCHAR(50) NOT NULL,
                        first_name      VARCHAR(50) NOT NULL,
                        middle_name     VARCHAR(50),
                        birth_date      DATE        NOT NULL CHECK (birth_date < CURRENT_DATE),
                        gender          CHAR(1)     CHECK (gender IN ('M', 'F')),
                        contact_info    VARCHAR(100),
                        created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- COACH
-- =============================================
CREATE TABLE coach (
                       person_id        INT          PRIMARY KEY REFERENCES person(person_id) ON DELETE CASCADE,
                       license_number   VARCHAR(30)  NOT NULL UNIQUE,
                       specialization   VARCHAR(100),
                       experience_years SMALLINT     NOT NULL DEFAULT 0 CHECK (experience_years >= 0)
);

-- =============================================
-- SPORT_RANK
-- =============================================
CREATE TABLE sport_rank (
                            rank_id       INT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            rank_name     VARCHAR(50)  NOT NULL UNIQUE,
                            min_distance DECIMAL(5,2) CHECK (min_distance > 0),
                            age_category  VARCHAR(30),
                            rank_order    SMALLINT     NOT NULL DEFAULT 1 CHECK (rank_order > 0)
);

-- =============================================
-- ATHLETE
-- =============================================
CREATE TABLE athlete (
                         person_id      INT         PRIMARY KEY REFERENCES person(person_id) ON DELETE CASCADE,
                         coach_id       INT         REFERENCES coach(person_id) ON DELETE SET NULL,
                         athlete_number VARCHAR(20) NOT NULL UNIQUE,
                         start_year     INT         NOT NULL CHECK (start_year <= EXTRACT(YEAR FROM CURRENT_DATE)),
                         current_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (current_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- =============================================
-- PHYSIOLOGICAL_RECORD
-- =============================================
CREATE TABLE physiological_record (
                                      record_id   BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      athlete_id  INT         NOT NULL REFERENCES athlete(person_id) ON DELETE CASCADE,
                                      measure_date DATE       NOT NULL CHECK (measure_date <= CURRENT_DATE),
                                      weight      DECIMAL(5,2) CHECK (weight > 30 AND weight < 150),
                                      height      INT          CHECK (height > 100 AND height < 250),
                                      vo2_max     DECIMAL(4,1),
                                      resting_hr  INT          CHECK (resting_hr > 30 AND resting_hr < 120),
                                      notes       VARCHAR(500)
);

-- =============================================
-- COMPETITION
-- =============================================
CREATE TABLE competition (
                             competition_id    INT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name       VARCHAR(100) NOT NULL,
                             location   VARCHAR(100) NOT NULL,
                             comp_date  DATE         NOT NULL,
                             hill_size  VARCHAR(20),
                             level      VARCHAR(20)  DEFAULT 'local' CHECK (level IN ('local','regional','national','international')),
                             max_participants SMALLINT CHECK (max_participants > 0),
                             status     VARCHAR(20)  NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','active','finished','cancelled'))
);

-- =============================================
-- JUMP_RESULT
-- =============================================
CREATE TABLE jump_result (
                             jump_id        BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             athlete_id     INT          NOT NULL REFERENCES athlete(person_id) ON DELETE RESTRICT,
                             competition_id INT          NOT NULL REFERENCES competition(competition_id) ON DELETE RESTRICT,
                             jump_timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             distance     DECIMAL(5,2) NOT NULL CHECK (distance > 0),
                             style_points   DECIMAL(4,2) CHECK (style_points >= 0 AND style_points <= 60),
                             wind_factor    DECIMAL(3,2),
                             gate_mark      VARCHAR(10)
);

-- =============================================
-- STANDARDS_MAP
-- Фиксирует факт выполнения норматива конкретным прыжком
-- =============================================
CREATE TABLE standards_map (
                               jump_id BIGINT NOT NULL REFERENCES jump_result(jump_id) ON DELETE CASCADE,
                               rank_id INT    NOT NULL REFERENCES sport_rank(rank_id)  ON DELETE CASCADE,
                               PRIMARY KEY (jump_id, rank_id)
);

-- =============================================
-- RECORD
-- =============================================
CREATE TABLE record (
                        record_id         INT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        jump_id           BIGINT      NOT NULL UNIQUE REFERENCES jump_result(jump_id) ON DELETE CASCADE,
                        record_type       VARCHAR(30) NOT NULL CHECK (record_type IN ('club','regional','national','international')),
                        verification_date DATE        NOT NULL DEFAULT CURRENT_DATE
);

-- =============================================
-- ИНДЕКСЫ
-- =============================================
CREATE INDEX idx_athlete_coach      ON athlete(coach_id);
CREATE INDEX idx_athlete_status     ON athlete(current_status);
CREATE INDEX idx_physio_athlete     ON physiological_record(athlete_id);
CREATE INDEX idx_physio_date        ON physiological_record(measure_date);
CREATE INDEX idx_jump_athlete       ON jump_result(athlete_id);
CREATE INDEX idx_jump_comp          ON jump_result(competition_id);
CREATE INDEX idx_jump_distance      ON jump_result(distance DESC);
CREATE INDEX idx_comp_date          ON competition(comp_date);
CREATE INDEX idx_standards_rank     ON standards_map(rank_id);
CREATE INDEX idx_person_last_name   ON person(last_name);

-- =============================================
-- ТЕСТОВЫЕ ДАННЫЕ
-- =============================================

INSERT INTO person (last_name, first_name, middle_name, birth_date, gender, contact_info) VALUES
                                                                                              ('Петров',   'Александр', 'Иванович',   '1975-03-15', 'M', 'petrov@club.ru'),
                                                                                              ('Соколова', 'Марина',    'Андреевна',  '1980-07-22', 'F', 'sokolova@club.ru'),
                                                                                              ('Горбунов', 'Дмитрий',   'Сергеевич',  '2005-11-10', 'M', 'gorbunov@mail.ru'),
                                                                                              ('Зимин',    'Алексей',   'Павлович',   '2006-04-03', 'M', 'zimin@mail.ru'),
                                                                                              ('Снегирева','Ольга',     'Николаевна', '2007-08-17', 'F', 'snegireva@mail.ru'),
                                                                                              ('Морозов',  'Иван',      'Денисович',  '2004-01-25', 'M', 'morozov@mail.ru');
                                                                                              ('Шульман',  'Игнат',     'Евдокимович',  '1988-04-18', 'M', 'ignat88@mail.ru');

INSERT INTO coach (person_id, license_number, specialization, experience_years) VALUES
                                                                                    (1, 'LIC-2005-001', 'Прыжки с трамплина', 20),
                                                                                    (2, 'LIC-2010-047', 'Прыжки с трамплина', 15);

INSERT INTO sport_rank (rank_name, min_distance, age_category, rank_order) VALUES
                                                                                 ('III разряд', 40.00, 'U14', 1),
                                                                                 ('II разряд',  55.00, 'U16', 2),
                                                                                 ('I разряд',   70.00, 'U18', 3),
                                                                                 ('КМС',        85.00, 'U21', 4),
                                                                                 ('МС',        100.00, 'OPEN', 5);

INSERT INTO athlete (person_id, coach_id, athlete_number, start_year, current_status) VALUES
                                                                                          (3, 1, 'ATH-2020-001', 2020, 'ACTIVE'),
                                                                                          (4, 1, 'ATH-2021-002', 2021, 'ACTIVE'),
                                                                                          (5, 2, 'ATH-2022-003', 2022, 'ACTIVE'),
                                                                                          (6, 2, 'ATH-2019-004', 2019, 'ACTIVE');
                                                                                          (7, 2, 'ATH-2000-005', 2020, 'INACTIVE');

INSERT INTO competition (name, location, comp_date, hill_size, level, max_participants, status) VALUES
                                                                                                    ('Открытый чемпионат клуба 2024',  'Пермь, трамплин К-90',  '2024-02-10', 'K-90',  'local',    30, 'finished'),
                                                                                                    ('Региональные соревнования 2024', 'Чайковский, трамплин',  '2024-03-15', 'K-90',  'regional', 50, 'finished'),
                                                                                                    ('Кубок Урала 2025',               'Нижний Тагил, К-120',   '2025-01-20', 'K-120', 'regional', 60, 'finished'),
                                                                                                    ('Чемпионат клуба 2025',           'Пермь, трамплин К-90',  '2025-12-10', 'K-90',  'local',    30, 'planned');

INSERT INTO jump_result (athlete_id, competition_id, jump_timestamp, distance, style_points, wind_factor, gate_mark) VALUES
                                                                                                                    (3, 1, '2024-02-10 10:15:00', 72.50, 51.5,  0.30, 'G10'),
                                                                                                                    (3, 1, '2024-02-10 14:30:00', 74.00, 53.0,  0.10, 'G10'),
                                                                                                                    (4, 1, '2024-02-10 10:45:00', 58.00, 45.0, -0.20, 'G8'),
                                                                                                                    (5, 1, '2024-02-10 11:00:00', 43.50, 42.0,  0.00, 'G7'),
                                                                                                                    (6, 2, '2024-03-15 11:20:00', 88.00, 57.0,  0.50, 'G12'),
                                                                                                                    (3, 2, '2024-03-15 11:45:00', 76.50, 54.5, -0.10, 'G10'),
                                                                                                                    (6, 3, '2025-01-20 12:00:00', 91.00, 58.5,  0.20, 'G12');

INSERT INTO physiological_record (athlete_id, measure_date, weight, height, vo2_max, resting_hr, notes) VALUES
                                                                                                            (3, '2024-01-15', 68.0, 175, 52.3, 58, 'Плановый замер'),
                                                                                                            (3, '2024-06-10', 67.5, 175, 53.1, 57, 'После летней подготовки'),
                                                                                                            (4, '2024-01-15', 62.0, 168, 49.8, 62, 'Плановый замер'),
                                                                                                            (5, '2024-02-01', 54.0, 160, 47.2, 65, 'Плановый замер'),
                                                                                                            (6, '2024-01-20', 74.0, 180, 55.6, 55, 'Плановый замер');

INSERT INTO standards_map (jump_id, rank_id) VALUES
                                                 (1, 3),
                                                 (2, 3),
                                                 (5, 4),
                                                 (7, 4);

INSERT INTO record (jump_id, record_type, verification_date) VALUES
                                                                 (5, 'club',     '2024-03-16'),
                                                                 (7, 'regional', '2025-01-21');