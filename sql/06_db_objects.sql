BEGIN;

-- helper: full years between birth date and the given date
CREATE FUNCTION fn_age_on(p_birth_date DATE, p_on DATE) RETURNS INT
LANGUAGE sql IMMUTABLE AS $$
    SELECT EXTRACT(YEAR FROM AGE(p_on, p_birth_date))::INT
$$;

-- one row per jump with athlete, age category, hill and calculated points
-- points = 60 + (distance - K-point) * meter value + style points + wind compensation
CREATE VIEW v_jump_context AS
SELECT jr.id AS jump_id,
       jr.athlete_id,
       p.last_name || ' ' || p.first_name AS athlete_name,
       p.gender,
       p.birth_date,
       jr.timestamp::DATE AS jump_date,
       fn_age_on(p.birth_date, jr.timestamp::DATE) AS age,
       ac.id AS age_category_id,
       ac.type AS age_category_type,
       ac.name AS age_category_name,
       jr.competition_id,
       c.name AS competition_name,
       h.id AS hill_id,
       h.type AS hill_type,
       h.name AS hill_name,
       h.k_point,
       h.meter_value,
       jr.distance,
       jr.style_points,
       jr.wind_factor,
       CASE
           WHEN h.id IS NULL THEN NULL
           ELSE ROUND(60 + (jr.distance - h.k_point) * h.meter_value + COALESCE(jr.style_points, 0) + COALESCE(jr.wind_factor, 0), 1)
       END AS points
FROM jump_result jr
JOIN person p ON p.id = jr.athlete_id
JOIN competition c ON c.id = jr.competition_id
LEFT JOIN hill h ON h.id = c.hill_id
LEFT JOIN age_category ac ON ac.age_range @> fn_age_on(p.birth_date, jr.timestamp::DATE);

-- best jump for every age category, gender and hill
CREATE VIEW v_age_records AS
SELECT DISTINCT ON (jc.age_category_id, jc.gender, jc.hill_id)
       jc.age_category_id,
       jc.age_category_type,
       jc.age_category_name,
       jc.gender,
       jc.hill_id,
       jc.hill_type,
       jc.hill_name,
       jc.athlete_id,
       jc.athlete_name,
       jc.distance,
       jc.jump_date,
       jc.competition_name,
       r.type AS record_type
FROM v_jump_context jc
LEFT JOIN record r ON r.jump_id = jc.jump_id
WHERE jc.age_category_id IS NOT NULL AND jc.hill_id IS NOT NULL
ORDER BY jc.age_category_id, jc.gender, jc.hill_id, jc.distance DESC, jc.jump_date;

-- readable table of norms
CREATE VIEW v_norms AS
SELECT ac.type AS age_category_type,
       ac.name AS age_category_name,
       ac.min_age,
       ac.max_age,
       n.gender,
       h.type AS hill_type,
       h.name AS hill_name,
       sr.type AS rank_type,
       sr.name AS rank_name,
       sr.sort_order AS rank_sort_order,
       n.min_distance
FROM norm n
JOIN age_category ac ON ac.id = n.age_category_id
JOIN hill h ON h.id = n.hill_id
JOIN sport_rank sr ON sr.id = n.rank_id;

-- progress of an athlete on a hill: previous jump, delta, best so far, moving average and body measures at that time
CREATE VIEW v_jump_dynamics AS
SELECT jc.athlete_id,
       jc.athlete_name,
       jc.jump_id,
       jc.jump_date,
       jc.hill_id,
       jc.hill_type,
       jc.distance,
       LAG(jc.distance) OVER w AS previous_distance,
       jc.distance - LAG(jc.distance) OVER w AS delta,
       MAX(jc.distance) OVER (w ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS best_so_far,
       ROUND(AVG(jc.distance) OVER (w ROWS BETWEEN 2 PRECEDING AND CURRENT ROW), 2) AS moving_avg_3,
       pr.height,
       pr.weight
FROM v_jump_context jc
LEFT JOIN LATERAL (
    SELECT p.height, p.weight
    FROM physiological_record p
    WHERE p.athlete_id = jc.athlete_id AND p.measure_date <= jc.jump_date
    ORDER BY p.measure_date DESC
    LIMIT 1
) pr ON TRUE
WINDOW w AS (PARTITION BY jc.athlete_id, jc.hill_id ORDER BY jc.jump_date, jc.jump_id);

-- correlation of height and weight with distance per hill
CREATE VIEW v_physio_distance_correlation AS
SELECT hill_id,
       hill_type,
       COUNT(*) AS jumps_count,
       ROUND(CORR(height, distance)::NUMERIC, 3) AS height_correlation,
       ROUND(CORR(weight, distance)::NUMERIC, 3) AS weight_correlation
FROM v_jump_dynamics
WHERE height IS NOT NULL AND weight IS NOT NULL
GROUP BY hill_id, hill_type;

-- protocol of a competition: jumps, best distance, total points and place
CREATE VIEW v_competition_protocol AS
SELECT jc.competition_id,
       jc.competition_name,
       jc.athlete_id,
       jc.athlete_name,
       COUNT(*) AS jumps_count,
       MAX(jc.distance) AS best_distance,
       SUM(jc.points) AS total_points,
       RANK() OVER (
           PARTITION BY jc.competition_id
           ORDER BY SUM(jc.points) DESC NULLS LAST, MAX(jc.distance) DESC
       ) AS place
FROM v_jump_context jc
GROUP BY jc.competition_id, jc.competition_name, jc.athlete_id, jc.athlete_name;

-- the highest assigned rank of every athlete
CREATE VIEW v_athlete_current_rank AS
SELECT DISTINCT ON (ar.athlete_id)
       ar.athlete_id,
       sr.id AS rank_id,
       sr.type AS rank_type,
       sr.name AS rank_name,
       ar.assigned_date
FROM athlete_rank ar
JOIN sport_rank sr ON sr.id = ar.rank_id
ORDER BY ar.athlete_id, sr.sort_order DESC, ar.assigned_date;

-- triggers
CREATE FUNCTION fn_validate_physiological_record() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    v_birth_date DATE;
BEGIN
    SELECT birth_date INTO v_birth_date FROM person WHERE id = NEW.athlete_id;
    IF NEW.measure_date < v_birth_date THEN
        RAISE EXCEPTION 'Measure date % is earlier than birth date % of athlete %', NEW.measure_date, v_birth_date, NEW.athlete_id
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_validate_physiological_record
    BEFORE INSERT OR UPDATE OF athlete_id, measure_date ON physiological_record
    FOR EACH ROW EXECUTE FUNCTION fn_validate_physiological_record();

CREATE FUNCTION fn_validate_athlete_start_year() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
DECLARE
    v_birth_year INT;
BEGIN
    SELECT EXTRACT(YEAR FROM birth_date)::INT INTO v_birth_year FROM person WHERE id = NEW.id;
    IF NEW.start_year < v_birth_year THEN
        RAISE EXCEPTION 'Start year % is earlier than birth year % of athlete %', NEW.start_year, v_birth_year, NEW.id
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_validate_athlete_start_year
    BEFORE INSERT OR UPDATE OF start_year ON athlete
    FOR EACH ROW EXECUTE FUNCTION fn_validate_athlete_start_year();

-- procedures
-- records which norms a jump fulfils and gives the athlete the highest of them if it is above the current rank
CREATE PROCEDURE p_assign_rank(p_jump_id BIGINT)
LANGUAGE plpgsql AS $$
DECLARE
    v_jump          v_jump_context%ROWTYPE;
    v_best_rank_id  BIGINT;
    v_best_order    SMALLINT;
    v_current_order SMALLINT;
BEGIN
    SELECT * INTO v_jump FROM v_jump_context WHERE jump_id = p_jump_id;
    IF NOT FOUND THEN
        RETURN;
    END IF;

    DELETE FROM standards_map WHERE jump_id = p_jump_id;

    IF v_jump.hill_id IS NULL OR v_jump.age_category_id IS NULL OR v_jump.gender IS NULL THEN
        RETURN;
    END IF;

    INSERT INTO standards_map (jump_id, rank_id)
    SELECT p_jump_id, n.rank_id
    FROM norm n
    WHERE n.age_category_id = v_jump.age_category_id
      AND n.gender = v_jump.gender
      AND n.hill_id = v_jump.hill_id
      AND n.min_distance <= v_jump.distance;

    SELECT sr.id, sr.sort_order INTO v_best_rank_id, v_best_order
    FROM standards_map sm
    JOIN sport_rank sr ON sr.id = sm.rank_id
    WHERE sm.jump_id = p_jump_id
    ORDER BY sr.sort_order DESC
    LIMIT 1;

    IF v_best_rank_id IS NULL THEN
        RETURN;
    END IF;

    SELECT COALESCE(MAX(sr.sort_order), 0) INTO v_current_order
    FROM athlete_rank ar
    JOIN sport_rank sr ON sr.id = ar.rank_id
    WHERE ar.athlete_id = v_jump.athlete_id;

    IF v_best_order > v_current_order THEN
        INSERT INTO athlete_rank (athlete_id, rank_id, assigned_date, jump_id)
        VALUES (v_jump.athlete_id, v_best_rank_id, v_jump.jump_date, p_jump_id)
        ON CONFLICT (athlete_id, rank_id) DO NOTHING;
    END IF;
END $$;

CREATE FUNCTION fn_jump_result_assign_rank() RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    CALL p_assign_rank(NEW.id);
    RETURN NULL;
END $$;

CREATE TRIGGER trg_jump_result_assign_rank
    AFTER INSERT OR UPDATE OF distance, athlete_id, competition_id ON jump_result
    FOR EACH ROW EXECUTE FUNCTION fn_jump_result_assign_rank();

-- linear trend of distance relative to K-point over time, projected on the last hill of the athlete
CREATE PROCEDURE p_calculate_forecast(
    IN p_athlete_id BIGINT,
    IN p_horizon_days INT,
    OUT forecast_distance NUMERIC,
    OUT trend_per_year NUMERIC,
    OUT jumps_used INT,
    OUT r_squared NUMERIC
)
LANGUAGE plpgsql AS $$
DECLARE
    v_slope     DOUBLE PRECISION;
    v_intercept DOUBLE PRECISION;
    v_r2        DOUBLE PRECISION;
    v_last_day  DOUBLE PRECISION;
    v_last_k    SMALLINT;
BEGIN
    IF p_horizon_days < 0 THEN
        RAISE EXCEPTION 'Forecast horizon must not be negative: %', p_horizon_days;
    END IF;

    SELECT REGR_SLOPE(s.y, s.x), REGR_INTERCEPT(s.y, s.x), REGR_R2(s.y, s.x), COUNT(*), MAX(s.x)
    INTO v_slope, v_intercept, v_r2, jumps_used, v_last_day
    FROM (
        SELECT (jump_date - DATE '1970-01-01')::DOUBLE PRECISION AS x, distance::DOUBLE PRECISION / k_point AS y
        FROM v_jump_context
        WHERE athlete_id = p_athlete_id AND k_point IS NOT NULL
    ) s;

    SELECT k_point INTO v_last_k
    FROM v_jump_context
    WHERE athlete_id = p_athlete_id AND k_point IS NOT NULL
    ORDER BY jump_date DESC, jump_id DESC
    LIMIT 1;

    IF v_slope IS NULL OR v_last_k IS NULL THEN
        RETURN;
    END IF;

    forecast_distance := GREATEST(0, ROUND(((v_intercept + v_slope * (v_last_day + p_horizon_days)) * v_last_k)::NUMERIC, 2));
    trend_per_year := ROUND((v_slope * 365 * v_last_k)::NUMERIC, 2);
    r_squared := ROUND(COALESCE(v_r2, 0)::NUMERIC, 3);
END $$;

-- registers all results of a competition in one call, so either all jumps are saved or none
CREATE PROCEDURE p_register_competition_results(p_competition_id BIGINT, p_results JSONB)
LANGUAGE plpgsql AS $$
DECLARE
    v_status VARCHAR(20);
BEGIN
    SELECT status INTO v_status FROM competition WHERE id = p_competition_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Competition % not found', p_competition_id;
    END IF;
    IF v_status NOT IN ('ACTIVE', 'FINISHED') THEN
        RAISE EXCEPTION 'Results can not be registered for competition % in status %', p_competition_id, v_status;
    END IF;
    IF JSONB_TYPEOF(p_results) <> 'array' OR JSONB_ARRAY_LENGTH(p_results) = 0 THEN
        RAISE EXCEPTION 'Results must be a non-empty JSON array';
    END IF;

    INSERT INTO jump_result (athlete_id, competition_id, timestamp, distance, style_points, wind_factor, attempt_number, gate_mark)
    SELECT (r ->> 'athlete_id')::BIGINT,
           p_competition_id,
           COALESCE((r ->> 'timestamp')::TIMESTAMP, CURRENT_TIMESTAMP),
           (r ->> 'distance')::NUMERIC,
           (r ->> 'style_points')::NUMERIC,
           (r ->> 'wind_factor')::NUMERIC,
           (r ->> 'attempt_number')::SMALLINT,
           r ->> 'gate_mark'
    FROM JSONB_ARRAY_ELEMENTS(p_results) AS r;
END $$;

-- bring norm fulfilment and ranks of the existing jumps in line with the norms
DO $$
DECLARE
    v_jump_id BIGINT;
BEGIN
    FOR v_jump_id IN SELECT id FROM jump_result ORDER BY timestamp, id LOOP
        CALL p_assign_rank(v_jump_id);
    END LOOP;
END $$;

COMMIT;
