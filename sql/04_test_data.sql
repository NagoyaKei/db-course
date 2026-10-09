BEGIN;

INSERT INTO person (first_name, last_name, middle_name, birth_date, gender, contact_info) VALUES
    ('Александр', 'Петров',    'Иванович',   '1975-03-15', 'M', 'petrov@club.ru'),
    ('Марина',    'Соколова',  'Андреевна',  '1980-07-22', 'F', 'sokolova@club.ru'),
    ('Дмитрий',   'Горбунов',  'Сергеевич',  '2005-11-10', 'M', 'gorbunov@mail.ru'),
    ('Алексей',   'Зимин',     'Павлович',   '2006-04-03', 'M', 'zimin@mail.ru'),
    ('Ольга',     'Снегирева', 'Николаевна', '2007-08-17', 'F', 'snegireva@mail.ru'),
    ('Иван',      'Морозов',   'Денисович',  '2004-01-25', 'M', 'morozov@mail.ru'),
    ('Владимир',  'Белоусов',  'Павлович',   '1946-07-14', 'M', 'belousov-vp@club.ru');

INSERT INTO coach (id, license_number, experience_years) VALUES
    (1, 'LIC-2005-001', 20),
    (2, 'LIC-2010-047', 15),
    (7, 'LIC-1970-121', 55);

INSERT INTO athlete (id, coach_id, number, start_year, current_status) VALUES
    (3, 1, 'ATH-2020-001', 2020, 'ACTIVE'),
    (4, 1, 'ATH-2021-002', 2021, 'ACTIVE'),
    (5, 2, 'ATH-2022-003', 2022, 'ACTIVE'),
    (6, 2, 'ATH-2019-004', 2019, 'ACTIVE'),
    (7, null, 'ATH-1960-005', 1960, 'INACTIVE');

INSERT INTO competition (name, date, location, hill_id, level, max_participants, status) VALUES
    ('Открытый чемпионат клуба 2024',  '2024-02-10', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    10, 'FINISHED'),
    ('Региональные соревнования 2024', '2024-03-15', 'Чайковский',           (SELECT id FROM hill WHERE type = 'K-90'),  'REGIONAL', 30, 'FINISHED'),
    ('Кубок Урала 2025',               '2025-01-20', 'Нижний Тагил, К-120',  (SELECT id FROM hill WHERE type = 'K-120'), 'REGIONAL', 30, 'FINISHED'),
    ('Чемпионат клуба 2026',           '2026-12-10', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    20, 'PLANNED');

INSERT INTO jump_result (athlete_id, competition_id, timestamp, distance, style_points, wind_factor, attempt_number, gate_mark) VALUES
    (3, 1, '2024-02-10 10:15:00', 72.50, 51.5,  0.30, 1, 'G10'),
    (3, 1, '2024-02-10 14:30:00', 74.00, 53.0,  0.10, 2, 'G10'),
    (4, 1, '2024-02-10 10:45:00', 58.00, 45.0, -0.20, 1, 'G8'),
    (5, 1, '2024-02-10 11:00:00', 43.50, 42.0,  0.00, 1, 'G7'),
    (6, 2, '2024-03-15 11:20:00', 88.00, 57.0,  0.50, 1, 'G12'),
    (3, 2, '2024-03-15 11:45:00', 76.50, 54.5, -0.10, 1, 'G10'),
    (6, 3, '2025-01-20 12:00:00', 91.00, 58.5,  0.20, 1, 'G12');

INSERT INTO physiological_record (athlete_id, measure_date, height, weight, vo2_max, resting_hr, notes) VALUES
    (3, '2024-01-15', 175.00, 68.00, 52.30, 58.00, 'Плановый замер'),
    (3, '2024-06-10', 175.00, 67.50, 53.10, 57.00, 'После летней подготовки'),
    (4, '2024-01-15', 168.00, 62.00, 49.80, 62.00, 'Плановый замер'),
    (5, '2024-02-01', 160.00, 54.00, 47.20, 65.00, 'Плановый замер'),
    (6, '2024-01-20', 180.00, 74.00, 55.60, 55.00, 'Плановый замер');

INSERT INTO record (jump_id, type, verification_date) VALUES
    (5, 'CLUB',     '2024-03-16'),
    (7, 'REGIONAL', '2025-01-21');

-- more coaches and athletes, the key to join them with person is contact_info
INSERT INTO person (first_name, last_name, middle_name, birth_date, gender, contact_info) VALUES
    ('Андрей',  'Кузнецов', 'Олегович',   '1982-05-20', 'M', 'kuznetsov@club.ru'),
    ('Елена',   'Орлова',   'Викторовна', '1988-11-03', 'F', 'orlova@club.ru'),
    ('Артем',   'Волков',   'Игоревич',   '2013-03-12', 'M', 'volkov@mail.ru'),
    ('Дарья',   'Лебедева', 'Андреевна',  '2012-06-05', 'F', 'lebedeva@mail.ru'),
    ('Никита',  'Орлов',    'Сергеевич',  '2010-06-20', 'M', 'orlov@mail.ru'),
    ('Ксения',  'Попова',   'Павловна',   '2009-11-02', 'F', 'popova@mail.ru'),
    ('Егор',    'Смирнов',  'Алексеевич', '2008-04-18', 'M', 'smirnov@mail.ru'),
    ('Анна',    'Морозова', 'Ильинична',  '2005-01-30', 'F', 'morozova@mail.ru'),
    ('Максим',  'Федоров',  'Дмитриевич', '2003-08-09', 'M', 'fedorov@mail.ru'),
    ('Игорь',   'Козлов',   'Петрович',   '1999-12-01', 'M', 'kozlov@mail.ru'),
    ('Татьяна', 'Новикова', 'Олеговна',   '1998-05-25', 'F', 'novikova@mail.ru'),
    ('Сергей',  'Павлов',   'Николаевич', '1992-02-14', 'M', 'pavlov@mail.ru'),
    ('Олег',    'Васильев', 'Романович',  '2000-10-10', 'M', 'vasilev@mail.ru');

INSERT INTO coach (id, license_number, experience_years)
SELECT p.id, v.license_number, v.experience_years
FROM (VALUES
    ('kuznetsov@club.ru', 'LIC-2014-088', 12),
    ('orlova@club.ru',    'LIC-2018-132', 8)
) AS v(contact_info, license_number, experience_years)
JOIN person p ON p.contact_info = v.contact_info;

INSERT INTO athlete (id, coach_id, number, start_year, current_status)
SELECT p.id, c.id, v.number, v.start_year, v.status
FROM (VALUES
    ('volkov@mail.ru',   'kuznetsov@club.ru',   'ATH-2021-006', 2021, 'ACTIVE'),
    ('lebedeva@mail.ru', 'orlova@club.ru',      'ATH-2021-007', 2021, 'ACTIVE'),
    ('orlov@mail.ru',    'kuznetsov@club.ru',   'ATH-2020-008', 2020, 'ACTIVE'),
    ('popova@mail.ru',   'orlova@club.ru',      'ATH-2019-009', 2019, 'ACTIVE'),
    ('smirnov@mail.ru',  'kuznetsov@club.ru',   'ATH-2018-010', 2018, 'ACTIVE'),
    ('morozova@mail.ru', 'petrov@club.ru',      'ATH-2022-011', 2022, 'ACTIVE'),
    ('fedorov@mail.ru',  'sokolova@club.ru',    'ATH-2016-012', 2016, 'ACTIVE'),
    ('kozlov@mail.ru',   'sokolova@club.ru',    'ATH-2012-013', 2012, 'ACTIVE'),
    ('novikova@mail.ru', 'orlova@club.ru',      'ATH-2013-014', 2013, 'ACTIVE'),
    ('pavlov@mail.ru',   'belousov-vp@club.ru', 'ATH-2010-015', 2010, 'INACTIVE'),
    ('vasilev@mail.ru',  'belousov-vp@club.ru', 'ATH-2014-016', 2014, 'SUSPENDED')
) AS v(athlete_email, coach_email, number, start_year, status)
JOIN person p ON p.contact_info = v.athlete_email
JOIN person cp ON cp.contact_info = v.coach_email
JOIN coach c ON c.id = cp.id;

INSERT INTO competition (name, date, location, hill_id, level, max_participants, status) VALUES
    ('Зимний кубок клуба 2023',         '2023-02-18', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED'),
    ('Первенство региона 2023',         '2023-03-26', 'Чайковский',           (SELECT id FROM hill WHERE type = 'K-90'),  'REGIONAL', 40, 'FINISHED'),
    ('Кубок открытия сезона 2023',      '2023-12-16', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED'),
    ('Кубок открытия сезона 2024',      '2024-12-14', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED'),
    ('Первенство Пермского края 2025',  '2025-02-23', 'Чайковский',           (SELECT id FROM hill WHERE type = 'K-90'),  'REGIONAL', 40, 'FINISHED'),
    ('Всероссийские соревнования 2025', '2025-03-29', 'Нижний Тагил, К-120',  (SELECT id FROM hill WHERE type = 'K-120'), 'NATIONAL', 60, 'FINISHED'),
    ('Новогодний турнир 2025',          '2025-12-13', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'CANCELLED'),
    ('Зимний кубок клуба 2026',         '2026-01-24', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED'),
    ('Кубок Урала 2026',                '2026-02-21', 'Нижний Тагил, К-120',  (SELECT id FROM hill WHERE type = 'K-120'), 'REGIONAL', 50, 'FINISHED'),
    ('Первенство региона 2026',         '2026-03-28', 'Чайковский',           (SELECT id FROM hill WHERE type = 'K-90'),  'REGIONAL', 40, 'FINISHED'),
    ('Осенний кубок клуба 2026',        '2026-09-13', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED');

-- storyline of every athlete: distance as a share of the K-point grows by gain_per_year with noise, body measures change with the trend
CREATE TEMP TABLE athlete_profile ON COMMIT DROP AS
SELECT a.id AS athlete_id, v.base_ratio, v.gain_per_year, v.noise, v.first_date, v.last_date, v.pause_from, v.pause_to, v.k120_from,
       v.measure_from, v.height0, v.height_gain, v.weight0, v.weight_gain, v.vo2_0, v.vo2_gain, v.hr0
FROM (VALUES
    ('ATH-2020-001', 0.80, 0.030, 0.035, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2025-01-01', DATE '2023-03-01', 174.0, 0.5, 67.0, 0.0, 52.0,  0.5, 58),
    ('ATH-2021-002', 0.64, 0.050, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2025-06-01', DATE '2023-03-01', 166.0, 1.5, 60.0, 1.0, 49.0,  0.7, 62),
    ('ATH-2022-003', 0.48, 0.060, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 158.0, 1.5, 52.0, 1.0, 46.0,  0.8, 65),
    ('ATH-2019-004', 0.96, 0.010, 0.035, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 180.0, 0.0, 74.0, 0.0, 55.0,  0.3, 55),
    ('ATH-2021-006', 0.42, 0.080, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 140.0, 6.0, 33.0, 4.0, 44.0,  1.5, 80),
    ('ATH-2021-007', 0.40, 0.070, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 146.0, 5.5, 36.0, 4.0, 42.0,  1.2, 82),
    ('ATH-2020-008', 0.55, 0.070, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2025-01-01', DATE '2023-03-01', 156.0, 6.0, 45.0, 5.0, 48.0,  1.5, 70),
    ('ATH-2019-009', 0.52, 0.050, 0.035, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 160.0, 2.5, 48.0, 2.5, 45.0,  1.0, 68),
    ('ATH-2018-010', 0.72, 0.080, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2025-01-01', DATE '2023-03-01', 172.0, 2.5, 60.0, 3.0, 52.0,  1.5, 62),
    ('ATH-2022-011', 0.62, 0.040, 0.040, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2025-06-01', DATE '2023-03-01', 165.0, 0.0, 55.0, 0.5, 46.0,  0.8, 64),
    ('ATH-2016-012', 0.80, 0.020, 0.040, DATE '2023-02-01', DATE '2026-12-31', DATE '2024-06-01', DATE '2025-06-30', DATE '2025-01-01', DATE '2023-03-01', 181.0, 0.0, 77.0, 0.5, 56.0, -0.3, 55),
    ('ATH-2012-013', 0.90, 0.035, 0.035, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2023-01-01', DATE '2023-03-01', 183.0, 0.0, 78.0, 0.0, 60.0, -0.2, 50),
    ('ATH-2013-014', 0.72, 0.020, 0.035, DATE '2023-02-01', DATE '2026-12-31', NULL,              NULL,              DATE '2024-01-01', DATE '2023-03-01', 170.0, 0.0, 60.0, 0.0, 52.0,  0.0, 58),
    ('ATH-2010-015', 0.82, -0.020, 0.040, DATE '2023-02-01', DATE '2025-03-31', NULL,             NULL,              NULL,              DATE '2023-03-01', 178.0, 0.0, 82.0, 1.0, 50.0, -0.8, 60),
    ('ATH-2014-016', 0.76, 0.020, 0.040, DATE '2023-02-01', DATE '2025-12-31', NULL,              NULL,              DATE '2025-01-01', DATE '2023-03-01', 179.0, 0.0, 75.0, 0.0, 54.0,  0.0, 57),
    ('ATH-1960-005', 1.15, -0.010, 0.030, DATE '2023-02-01', DATE '2025-03-31', NULL,              NULL,              NULL,              DATE '2023-03-01', 176.0, 0.0, 80.0, 0.5, 38.0, -0.5, 66)
) AS v(number, base_ratio, gain_per_year, noise, first_date, last_date, pause_from, pause_to, k120_from,
       measure_from, height0, height_gain, weight0, weight_gain, vo2_0, vo2_gain, hr0)
JOIN athlete a ON a.number = v.number;

-- two attempts per competition, the pseudo random noise is a sine of the ids, so the data is the same on every run
INSERT INTO jump_result (athlete_id, competition_id, timestamp, distance, style_points, wind_factor, attempt_number, gate_mark)
SELECT s.athlete_id,
       s.competition_id,
       s.date + TIME '10:00' + (s.attempt - 1) * INTERVAL '3 hours' + (s.athlete_id % 20) * INTERVAL '4 minutes',
       ROUND((s.k_point * s.ratio)::NUMERIC, 2),
       LEAST(58, GREATEST(30, ROUND((36 + 22 * (s.ratio - 0.5) + 3 * SIN(s.athlete_id * 3.7 + s.competition_id * 5.3 + s.attempt))::NUMERIC, 1))),
       ROUND((1.5 * SIN(s.athlete_id * 7.1 + s.competition_id * 2.9 + s.attempt * 4.3))::NUMERIC, 2),
       s.attempt,
       'G' || (6 + s.athlete_id % 6)
FROM (
    SELECT p.athlete_id, c.id AS competition_id, c.date, h.k_point, t.attempt,
           GREATEST(0.2, p.base_ratio + p.gain_per_year * (c.date - DATE '2023-01-01') / 365.0
               + p.noise * SIN(p.athlete_id * 12.9898 + c.id * 78.233 + t.attempt * 37.719)) AS ratio
    FROM athlete_profile p
    JOIN competition c ON c.status = 'FINISHED' AND c.date BETWEEN p.first_date AND p.last_date
    JOIN hill h ON h.id = c.hill_id
    CROSS JOIN (VALUES (1), (2)) AS t(attempt)
    WHERE (h.k_point < 100 OR c.date >= COALESCE(p.k120_from, DATE '9999-12-31'))
      AND NOT (c.date BETWEEN COALESCE(p.pause_from, DATE '9999-12-31') AND COALESCE(p.pause_to, DATE '9999-12-31'))
      AND (p.athlete_id + c.id) % 5 <> 0
      AND c.max_participants >= 20
      AND NOT EXISTS (SELECT 1 FROM jump_result j WHERE j.athlete_id = p.athlete_id AND j.competition_id = c.id)
) s
ORDER BY s.date, s.competition_id, s.athlete_id, s.attempt;

-- a body measurement every four months
INSERT INTO physiological_record (athlete_id, measure_date, height, weight, vo2_max, resting_hr, notes)
SELECT p.athlete_id,
       m.measure_date,
       ROUND((p.height0 + p.height_gain * m.years)::NUMERIC, 1),
       ROUND((p.weight0 + p.weight_gain * m.years + 0.8 * SIN(p.athlete_id * 2.3 + m.years * 9.1))::NUMERIC, 1),
       ROUND((p.vo2_0 + p.vo2_gain * m.years + 0.6 * SIN(p.athlete_id * 4.1 + m.years * 7.7))::NUMERIC, 1),
       ROUND((p.hr0 - m.years + 1.5 * SIN(p.athlete_id * 5.9 + m.years * 6.3))::NUMERIC, 0),
       'Плановый замер'
FROM athlete_profile p
CROSS JOIN LATERAL (
    SELECT d::DATE AS measure_date, (d::DATE - p.measure_from) / 365.0 AS years
    FROM GENERATE_SERIES(p.measure_from::TIMESTAMP, LEAST(p.last_date + 60, DATE '2026-09-01')::TIMESTAMP, INTERVAL '4 months') AS d
) m
ON CONFLICT (athlete_id, measure_date) DO NOTHING;

-- regional and national records first: the best jump of every hill at a competition of that level
INSERT INTO record (jump_id, type, verification_date)
SELECT DISTINCT ON (c.level, h.id) jr.id, c.level, jr.timestamp::DATE + 1
FROM jump_result jr
JOIN competition c ON c.id = jr.competition_id
JOIN hill h ON h.id = c.hill_id
WHERE c.level IN ('REGIONAL', 'NATIONAL')
ORDER BY c.level, h.id, jr.distance DESC, jr.timestamp
ON CONFLICT (jump_id) DO NOTHING;

-- club record: the best jump of every age category, gender and hill that is not a higher record yet
INSERT INTO record (jump_id, type, verification_date)
SELECT DISTINCT ON (ac.id, p.gender, h.id) jr.id, 'CLUB', jr.timestamp::DATE + 1
FROM jump_result jr
JOIN person p ON p.id = jr.athlete_id
JOIN competition c ON c.id = jr.competition_id
JOIN hill h ON h.id = c.hill_id
JOIN age_category ac ON ac.age_range @> EXTRACT(YEAR FROM AGE(jr.timestamp::DATE, p.birth_date))::INT
ORDER BY ac.id, p.gender, h.id, jr.distance DESC, jr.timestamp
ON CONFLICT (jump_id) DO NOTHING;

COMMIT;
