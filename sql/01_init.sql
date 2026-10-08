-- sql/01_init.sql

-- =============================================
-- AGE_CATEGORY
-- Справочник возрастных категорий.
-- Вынесен отдельно - устраняет транзитивную зависимость
-- в SPORT_RANK (rank_name -> age_range), что соответствует 3НФ.
-- =============================================
CREATE TABLE age_category (
                              id      BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              name    VARCHAR(50),
                              min_age SMALLINT     CHECK (min_age >= 0),
                              max_age SMALLINT     CHECK (max_age >= 0),
                              CONSTRAINT chk_age_range CHECK (min_age IS NULL OR max_age IS NULL OR min_age < max_age)
);

-- =============================================
-- SPORT_RANK
-- Каждый разряд однозначно определяется своим rank_id.
-- min_distance и age_category_id зависят только от rank_id - 3НФ соблюдена.
-- =============================================
CREATE TABLE sport_rank (
                            rank_id          BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            rank_name        VARCHAR(100)  NOT NULL UNIQUE,
                            min_distance     DECIMAL(5,2)  NOT NULL CHECK (min_distance > 0),
                            age_category_id  BIGINT        REFERENCES age_category(id) ON DELETE SET NULL,
                            rank_order       SMALLINT      NOT NULL DEFAULT 1 CHECK (rank_order BETWEEN 1 AND 10)
);

-- =============================================
-- PERSON
-- Супертип. Хранит только атрибуты общие для всех физических лиц.
-- Разделение на подтипы ATHLETE/COACH устраняет частичные зависимости - 2НФ.
-- =============================================
CREATE TABLE person (
                        person_id    BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        first_name   VARCHAR(100) NOT NULL,
                        last_name    VARCHAR(100) NOT NULL,
                        middle_name  VARCHAR(100),
                        birth_date   DATE         NOT NULL CHECK (birth_date < CURRENT_DATE),
                        gender       CHAR(1)      CHECK (gender IN ('M', 'F')),
                        contact_info VARCHAR(200),
                        created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- COACH
-- Подтип PERSON. person_id одновременно PK и FK -
-- связь 1:1 с PERSON без дублирования персональных данных.
-- =============================================
CREATE TABLE coach (
                       person_id        BIGINT       PRIMARY KEY REFERENCES person(person_id) ON DELETE CASCADE,
                       license_number   VARCHAR(50)  NOT NULL UNIQUE,
                       experience_years SMALLINT     NOT NULL DEFAULT 0 CHECK (experience_years >= 0)
);

-- =============================================
-- ATHLETE
-- Подтип PERSON. coach_id допускает NULL - спортсмен
-- может временно не иметь тренера (ON DELETE SET NULL).
-- athlete_number вынесен сюда, а не в PERSON, так как
-- атрибут специфичен только для спортсменов - 2НФ.
-- =============================================
CREATE TABLE athlete (
                         person_id      BIGINT      PRIMARY KEY REFERENCES person(person_id) ON DELETE CASCADE,
                         coach_id       BIGINT      REFERENCES coach(person_id) ON DELETE SET NULL,
                         athlete_number VARCHAR(20) NOT NULL UNIQUE,
                         start_year     SMALLINT    NOT NULL CHECK (start_year >= 2000 AND start_year <= EXTRACT(YEAR FROM CURRENT_DATE)),
                         current_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (current_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- =============================================
-- PHYSIOLOGICAL_RECORD
-- Историческая таблица замеров.
-- Каждый атрибут (height, weight, vo2_max, resting_hr)
-- зависит только от (athlete_id, measure_date) - 2НФ и 3НФ соблюдены.
-- Составной уникальный ключ исключает дублирование замеров в одну дату.
-- =============================================
CREATE TABLE physiological_record (
                                      record_id    BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      athlete_id   BIGINT        NOT NULL REFERENCES athlete(person_id) ON DELETE CASCADE,
                                      measure_date DATE          NOT NULL CHECK (measure_date <= CURRENT_DATE),
                                      height       DECIMAL(5,2)  NOT NULL CHECK (height > 1 AND height <= 300),
                                      weight       DECIMAL(5,2)  NOT NULL CHECK (weight > 1 AND weight <= 300),
                                      vo2_max      DECIMAL(5,2)  CHECK (vo2_max > 0),
                                      resting_hr   DECIMAL(5,2)  CHECK (resting_hr > 0),
                                      notes        VARCHAR(500),
                                      CONSTRAINT uq_athlete_measure UNIQUE (athlete_id, measure_date)
);

-- =============================================
-- COMPETITION
-- Все атрибуты зависят только от id соревнования - BCNF соблюдена.
-- hill_size специфичен для прыжков с трамплина и не выносится
-- в отдельный справочник, так как не имеет зависимых атрибутов.
-- =============================================
CREATE TABLE competition (
                             id               BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name             VARCHAR(200) NOT NULL,
                             competition_date DATE         NOT NULL,
                             location         VARCHAR(200),
                             hill_size        VARCHAR(20),
                             level            VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
                                 CHECK (level IN ('LOCAL','REGIONAL','NATIONAL','INTERNATIONAL')),
                             max_participants SMALLINT     CHECK (max_participants > 0),
                             status           VARCHAR(20)  NOT NULL DEFAULT 'planned'
                                 CHECK (status IN ('planned','active','finished','cancelled'))
);

-- =============================================
-- JUMP_RESULT
-- Ассоциативная сущность между ATHLETE и COMPETITION.
-- Все атрибуты прыжка зависят только от jump_id - BCNF.
-- ON DELETE RESTRICT защищает архивные данные от удаления.
-- =============================================
CREATE TABLE jump_result (
                             jump_id        BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             athlete_id     BIGINT        NOT NULL REFERENCES athlete(person_id) ON DELETE RESTRICT,
                             competition_id BIGINT        NOT NULL REFERENCES competition(id)    ON DELETE RESTRICT,
                             jump_timestamp TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             distance       DECIMAL(5,2)  NOT NULL CHECK (distance > 0 AND distance <= 300),
                             style_points   DECIMAL(5,2)  CHECK (style_points >= 0 AND style_points <= 60),
                             wind_factor    DECIMAL(4,2)  CHECK (wind_factor BETWEEN -99.99 AND 99.99),
                             attempt_number SMALLINT      CHECK (attempt_number >= 1),
                             gate_mark      VARCHAR(10)
);

-- =============================================
-- STANDARDS_MAP
-- Связующая таблица JUMP_RESULT - SPORT_RANK.
-- Составной PK (jump_id, rank_id) является суперключом -
-- BCNF соблюдена, транзитивных зависимостей нет.
-- Фиксирует факт выполнения норматива конкретным прыжком.
-- =============================================
CREATE TABLE standards_map (
                               jump_id BIGINT NOT NULL REFERENCES jump_result(jump_id) ON DELETE CASCADE,
                               rank_id BIGINT NOT NULL REFERENCES sport_rank(rank_id)  ON DELETE CASCADE,
                               PRIMARY KEY (jump_id, rank_id)
);

-- =============================================
-- RECORD
-- Фиксирует рекордные прыжки.
-- UNIQUE на jump_id гарантирует связь 1:1 с JUMP_RESULT.
-- Атрибуты record_type и verification_date зависят
-- только от record_id - 3НФ и BCNF соблюдены.
-- =============================================
CREATE TABLE record (
                        record_id         BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        jump_id           BIGINT      NOT NULL UNIQUE REFERENCES jump_result(jump_id) ON DELETE CASCADE,
                        record_type       VARCHAR(30) NOT NULL CHECK (record_type IN ('club','regional','national','international')),
                        verification_date DATE        NOT NULL DEFAULT CURRENT_DATE
);

-- =============================================
-- ИНДЕКСЫ
-- =============================================
CREATE INDEX idx_person_last_name        ON person(last_name);
CREATE INDEX idx_person_birth_date       ON person(birth_date);
CREATE INDEX idx_athlete_coach           ON athlete(coach_id);
CREATE INDEX idx_athlete_status          ON athlete(current_status);
CREATE INDEX idx_physio_athlete_date     ON physiological_record(athlete_id, measure_date);
CREATE INDEX idx_jump_athlete            ON jump_result(athlete_id);
CREATE INDEX idx_jump_competition        ON jump_result(competition_id);
CREATE INDEX idx_jump_distance           ON jump_result(distance DESC);
CREATE INDEX idx_competition_date        ON competition(competition_date);
CREATE INDEX idx_standards_rank          ON standards_map(rank_id);
CREATE INDEX idx_sport_rank_order        ON sport_rank(rank_order);
