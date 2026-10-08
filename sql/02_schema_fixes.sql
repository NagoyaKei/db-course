BEGIN;

-- columns carry no table-name prefix: id instead of rank_id, person_id, jump_id, record_id and so on
ALTER TABLE person               RENAME COLUMN person_id        TO id;
ALTER TABLE coach                RENAME COLUMN person_id        TO id;
ALTER TABLE athlete              RENAME COLUMN person_id        TO id;
ALTER TABLE athlete              RENAME COLUMN athlete_number   TO number;
ALTER TABLE athlete              RENAME CONSTRAINT athlete_athlete_number_key TO athlete_number_key;
ALTER TABLE physiological_record RENAME COLUMN record_id        TO id;
ALTER TABLE competition          RENAME COLUMN competition_date TO date;
ALTER TABLE jump_result          RENAME COLUMN jump_id          TO id;
ALTER TABLE jump_result          RENAME COLUMN jump_timestamp   TO timestamp;
ALTER TABLE record               RENAME COLUMN record_id        TO id;
ALTER TABLE record               RENAME COLUMN record_type      TO type;

-- reference tables: type is a UPPER_CASE English identifier, name is the Russian title
ALTER TABLE sport_rank RENAME COLUMN rank_id    TO id;
ALTER TABLE sport_rank RENAME COLUMN rank_name  TO type;
ALTER TABLE sport_rank RENAME COLUMN rank_order TO sort_order;
ALTER TABLE sport_rank RENAME CONSTRAINT sport_rank_rank_name_key TO sport_rank_type_key;
ALTER TABLE sport_rank ADD COLUMN name VARCHAR(100) NOT NULL;

-- age_category: non-overlapping inclusive ranges, so one age maps to exactly one category
ALTER TABLE age_category RENAME COLUMN name TO type;

ALTER TABLE age_category
    ALTER COLUMN type    SET NOT NULL,
    ALTER COLUMN min_age SET NOT NULL,
    ADD COLUMN name VARCHAR(100) NOT NULL,
    ADD CONSTRAINT uq_age_category_type UNIQUE (type);

ALTER TABLE age_category
    ADD COLUMN age_range INT4RANGE GENERATED ALWAYS AS (INT4RANGE(min_age, max_age, '[]')) STORED;

ALTER TABLE age_category
    ADD CONSTRAINT excl_age_category_overlap EXCLUDE USING gist (age_range WITH &&);

-- club members may have started long before 2000
ALTER TABLE athlete
    DROP CONSTRAINT athlete_start_year_check,
    ADD CONSTRAINT athlete_start_year_check CHECK (start_year >= 1900 AND start_year <= EXTRACT(YEAR FROM CURRENT_DATE));

-- constants are stored as UPPER_CASE English identifiers
ALTER TABLE competition
    DROP CONSTRAINT competition_status_check,
    ALTER COLUMN status SET DEFAULT 'PLANNED',
    ADD CONSTRAINT competition_status_check CHECK (status IN ('PLANNED', 'ACTIVE', 'FINISHED', 'CANCELLED'));

ALTER TABLE record
    DROP CONSTRAINT record_record_type_check,
    ADD CONSTRAINT record_type_check CHECK (type IN ('CLUB', 'REGIONAL', 'NATIONAL', 'INTERNATIONAL'));

-- hill: distances on different hills are not comparable, so norms and records need the hill
CREATE TABLE hill (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    k_point     SMALLINT     NOT NULL CHECK (k_point > 0),
    meter_value DECIMAL(4,2) NOT NULL CHECK (meter_value > 0)
);

-- one photo per person is stored in the database, so it is backed up and restored together with the data
CREATE TABLE person_image (
    person_id    BIGINT      PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
    content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp')),
    data         BYTEA       NOT NULL CHECK (OCTET_LENGTH(data) <= 2097152)
);

ALTER TABLE competition
    DROP COLUMN hill_size,
    ADD COLUMN hill_id BIGINT REFERENCES hill(id) ON DELETE RESTRICT;

CREATE INDEX idx_competition_hill ON competition(hill_id);

-- norm: minimal distance for a rank depends on age category, gender and hill
CREATE TABLE norm (
    id              BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rank_id         BIGINT       NOT NULL REFERENCES sport_rank(id)           ON DELETE RESTRICT,
    age_category_id BIGINT       NOT NULL REFERENCES age_category(id)         ON DELETE RESTRICT,
    gender          CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F')),
    hill_id         BIGINT       NOT NULL REFERENCES hill(id)                 ON DELETE RESTRICT,
    min_distance    DECIMAL(5,2) NOT NULL CHECK (min_distance > 0),
    CONSTRAINT uq_norm UNIQUE (rank_id, age_category_id, gender, hill_id)
);

CREATE INDEX idx_norm_lookup ON norm(age_category_id, gender, hill_id);

ALTER TABLE sport_rank
    DROP COLUMN min_distance,
    DROP COLUMN age_category_id;

-- athlete_rank: history of rank assignments
CREATE TABLE athlete_rank (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    athlete_id    BIGINT NOT NULL REFERENCES athlete(id)           ON DELETE RESTRICT,
    rank_id       BIGINT NOT NULL REFERENCES sport_rank(id)        ON DELETE RESTRICT,
    assigned_date DATE   NOT NULL,
    jump_id       BIGINT REFERENCES jump_result(id)                ON DELETE SET NULL,
    CONSTRAINT uq_athlete_rank UNIQUE (athlete_id, rank_id)
);

CREATE INDEX idx_athlete_rank_athlete ON athlete_rank(athlete_id, assigned_date);

COMMIT;
