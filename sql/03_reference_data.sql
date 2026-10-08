BEGIN;

INSERT INTO age_category (type, name, min_age, max_age) VALUES
    ('U14',  'До 14 лет',          8,   13),
    ('U18',  '14-17 лет',         14,   17),
    ('U21',  '18-20 лет',         18,   20),
    ('OPEN', 'Открытая категория', 21, NULL);

INSERT INTO hill (type, name, k_point, meter_value) VALUES
    ('K-90',  'Трамплин K-90',   90, 2.00),
    ('K-120', 'Трамплин K-120', 120, 1.80);

INSERT INTO sport_rank (type, name, sort_order) VALUES
    ('THIRD_CLASS',  'III разряд',                1),
    ('SECOND_CLASS', 'II разряд',                 2),
    ('FIRST_CLASS',  'I разряд',                  3),
    ('CMS',          'Кандидат в мастера спорта', 4),
    ('MS',           'Мастер спорта',             5);

-- test norms: base distance is the male norm on K-90, scaled by K-point and gender
INSERT INTO norm (rank_id, age_category_id, gender, hill_id, min_distance)
SELECT r.id, a.id, g.gender, h.id, ROUND(base.distance * h.k_point / 90.0 * g.factor, 0)
FROM sport_rank r
JOIN (VALUES ('THIRD_CLASS', 40), ('SECOND_CLASS', 55), ('FIRST_CLASS', 70), ('CMS', 85), ('MS', 100)) AS base(type, distance)
    ON base.type = r.type
JOIN (VALUES ('U14', 2), ('U18', 3), ('U21', 4), ('OPEN', 5)) AS cap(type, max_sort_order)
    ON r.sort_order <= cap.max_sort_order
JOIN age_category a ON a.type = cap.type
CROSS JOIN (VALUES ('M', 1.00), ('F', 0.90)) AS g(gender, factor)
CROSS JOIN hill h;

COMMIT;
