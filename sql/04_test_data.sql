BEGIN;

INSERT INTO person (first_name, last_name, middle_name, birth_date, gender, contact_info) VALUES
    ('Александр', 'Петров',    'Иванович',   '1975-03-15', 'M', 'petrov@club.ru'),
    ('Марина',    'Соколова',  'Андреевна',  '1980-07-22', 'F', 'sokolova@club.ru'),
    ('Дмитрий',   'Горбунов',  'Сергеевич',  '2005-11-10', 'M', 'gorbunov@mail.ru'),
    ('Алексей',   'Зимин',     'Павлович',   '2006-04-03', 'M', 'zimin@mail.ru'),
    ('Ольга',     'Снегирева', 'Николаевна', '2007-08-17', 'F', 'snegireva@mail.ru'),
    ('Иван',      'Морозов',   'Денисович',  '2004-01-25', 'M', 'morozov@mail.ru');

INSERT INTO coach (id, license_number, specialization, experience_years) VALUES
    (1, 'LIC-2005-001', 'Прыжки с трамплина', 20),
    (2, 'LIC-2010-047', 'Прыжки с трамплина', 15);

INSERT INTO athlete (id, coach_id, number, start_year, current_status) VALUES
    (3, 1, 'ATH-2020-001', 2020, 'ACTIVE'),
    (4, 1, 'ATH-2021-002', 2021, 'ACTIVE'),
    (5, 2, 'ATH-2022-003', 2022, 'ACTIVE'),
    (6, 2, 'ATH-2019-004', 2019, 'ACTIVE');

INSERT INTO competition (name, date, location, hill_id, level, max_participants, status) VALUES
    ('Открытый чемпионат клуба 2024',  '2024-02-10', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'FINISHED'),
    ('Региональные соревнования 2024', '2024-03-15', 'Чайковский',           (SELECT id FROM hill WHERE type = 'K-90'),  'REGIONAL', 50, 'FINISHED'),
    ('Кубок Урала 2025',               '2025-01-20', 'Нижний Тагил, К-120',  (SELECT id FROM hill WHERE type = 'K-120'), 'REGIONAL', 60, 'FINISHED'),
    ('Чемпионат клуба 2026',           '2026-12-10', 'Пермь, трамплин К-90', (SELECT id FROM hill WHERE type = 'K-90'),  'LOCAL',    30, 'PLANNED');

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

INSERT INTO standards_map (jump_id, rank_id) VALUES
    (1, 3),
    (2, 3),
    (5, 4),
    (7, 4);

INSERT INTO record (jump_id, type, verification_date) VALUES
    (5, 'CLUB',     '2024-03-16'),
    (7, 'REGIONAL', '2025-01-21');

INSERT INTO athlete_rank (athlete_id, rank_id, assigned_date, jump_id) VALUES
    (3, 3, '2024-02-10', 1),
    (6, 4, '2024-03-15', 5);

COMMIT;
