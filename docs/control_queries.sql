-- Control queries for the course work, run after sql/01..05:
--   psql -U gorbunov -d gorbunov_db -f docs/control_queries.sql

-- K1. Row counts of all tables
SELECT 'age_category' AS table_name, COUNT(*) AS rows FROM age_category
UNION ALL SELECT 'hill', COUNT(*) FROM hill
UNION ALL SELECT 'sport_rank', COUNT(*) FROM sport_rank
UNION ALL SELECT 'norm', COUNT(*) FROM norm
UNION ALL SELECT 'person', COUNT(*) FROM person
UNION ALL SELECT 'person_image', COUNT(*) FROM person_image
UNION ALL SELECT 'coach', COUNT(*) FROM coach
UNION ALL SELECT 'athlete', COUNT(*) FROM athlete
UNION ALL SELECT 'physiological_record', COUNT(*) FROM physiological_record
UNION ALL SELECT 'competition', COUNT(*) FROM competition
UNION ALL SELECT 'jump_result', COUNT(*) FROM jump_result
UNION ALL SELECT 'athlete_rank', COUNT(*) FROM athlete_rank
UNION ALL SELECT 'standards_map', COUNT(*) FROM standards_map
UNION ALL SELECT 'record', COUNT(*) FROM record;

-- K2. Records by age category, gender and hill
SELECT age_category_name, gender, hill_type, athlete_name, distance, jump_date, record_type
FROM v_age_records
ORDER BY hill_type, age_category_id, gender;

-- K3. Dynamics of one athlete on the K-90 hill: previous jump, delta and moving average of three jumps
SELECT jump_date, distance, previous_distance, delta, best_so_far, moving_avg_3
FROM v_jump_dynamics
WHERE athlete_id = (SELECT id FROM athlete WHERE number = 'ATH-2020-001') AND hill_type = 'K-90'
ORDER BY jump_date;

-- K4. Norms table for adults on K-90
SELECT rank_name, gender, min_distance
FROM v_norms
WHERE age_category_type = 'OPEN' AND hill_type = 'K-90'
ORDER BY gender, rank_sort_order;

-- K5. Athletes by physiology: latest measurement with height 175-185 cm and weight 70-80 kg
SELECT p.last_name, p.first_name, r.measure_date, r.height, r.weight
FROM athlete a
JOIN person p ON p.id = a.id
JOIN LATERAL (
    SELECT measure_date, height, weight FROM physiological_record
    WHERE athlete_id = a.id ORDER BY measure_date DESC LIMIT 1
) r ON TRUE
WHERE r.height BETWEEN 175 AND 185 AND r.weight BETWEEN 70 AND 80
ORDER BY p.last_name;

-- K6. Protocol of one competition with places (window function RANK)
SELECT place, athlete_name, jumps_count, best_distance, total_points
FROM v_competition_protocol
WHERE competition_id = (SELECT id FROM competition WHERE name = 'Всероссийские соревнования 2025')
ORDER BY place;

-- K7. Forecast of the distance in 30, 60 and 90 days (procedure with OUT parameters, CALL takes no subquery, so psql variable)
SELECT id AS forecast_athlete FROM athlete WHERE number = 'ATH-2020-001' \gset
CALL p_calculate_forecast(:forecast_athlete, 30, NULL, NULL, NULL, NULL);
CALL p_calculate_forecast(:forecast_athlete, 60, NULL, NULL, NULL, NULL);
CALL p_calculate_forecast(:forecast_athlete, 90, NULL, NULL, NULL, NULL);

-- K8. Fulfilled norms of one athlete with the measurement of that day
SELECT rank_name, hill_type, jump_date, distance, measure_date, height, weight
FROM v_norm_fulfilment
WHERE athlete_id = (SELECT id FROM athlete WHERE number = 'ATH-1960-005')
ORDER BY rank_sort_order, hill_type;

-- K9. Correlation of height and weight with the distance by hill
SELECT hill_type, jumps_count, height_correlation, weight_correlation
FROM v_physio_distance_correlation
ORDER BY hill_type;

-- K10. Current ranks of athletes
SELECT COALESCE(cr.rank_name, 'не присвоен') AS rank_name, COUNT(*) AS athletes
FROM athlete a
LEFT JOIN v_athlete_current_rank cr ON cr.athlete_id = a.id
GROUP BY cr.rank_name
ORDER BY MIN(COALESCE(cr.rank_id, 0));

-- K11. Coaches who are also athletes
SELECT p.last_name, p.first_name, c.license_number, a.number, a.current_status
FROM coach c
JOIN person p ON p.id = c.id
LEFT JOIN athlete a ON a.id = c.id
ORDER BY p.last_name;

-- K12. Plan of the query on the jumps of one athlete (index idx_jump_athlete)
EXPLAIN (ANALYZE, COSTS OFF, TIMING OFF)
SELECT * FROM jump_result WHERE athlete_id = (SELECT id FROM athlete WHERE number = 'ATH-2020-001') ORDER BY distance DESC;
