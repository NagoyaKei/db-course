-- sql/01_init.sql

-- =============================================
-- AGE_CATEGORY
-- Справочник возрастных категорий (type - код, name - название).
-- Вынесен отдельно - устраняет транзитивную зависимость
-- в SPORT_RANK (rank_name -> age_range), что соответствует 3НФ.
-- Диапазоны [min_age, max_age] включительно и не пересекаются,
-- поэтому каждый возраст относится ровно к одной категории.
-- =============================================
CREATE TABLE age_category (
                              id        BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              type      VARCHAR(50)  NOT NULL,
                              name      VARCHAR(100) NOT NULL,
                              min_age   SMALLINT     NOT NULL CHECK (min_age >= 0),
                              max_age   SMALLINT     CHECK (max_age >= 0),
                              age_range INT4RANGE    GENERATED ALWAYS AS (INT4RANGE(min_age, max_age, '[]')) STORED,
                              CONSTRAINT uq_age_category_type UNIQUE (type),
                              CONSTRAINT chk_age_range CHECK (min_age IS NULL OR max_age IS NULL OR min_age < max_age),
                              CONSTRAINT excl_age_category_overlap EXCLUDE USING gist (age_range WITH &&)
);

-- =============================================
-- HILL
-- Справочник типоразмеров трамплина. Дистанции на разных
-- трамплинах несравнимы, поэтому нормативы и рекорды учитывают трамплин.
-- =============================================
CREATE TABLE hill (
                      id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      type        VARCHAR(20)  NOT NULL UNIQUE,
                      name        VARCHAR(100) NOT NULL,
                      k_point     SMALLINT     NOT NULL CHECK (k_point > 0),
                      meter_value DECIMAL(4,2) NOT NULL CHECK (meter_value > 0)
);

-- =============================================
-- SPORT_RANK
-- Справочник разрядов (type - код UPPER_CASE, name - название).
-- Норматив разряда зависит от возрастной категории, пола и
-- трамплина, поэтому хранится в NORM, а не здесь.
-- =============================================
CREATE TABLE sport_rank (
                            id         BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            type       VARCHAR(100)  NOT NULL UNIQUE,
                            name       VARCHAR(100)  NOT NULL,
                            sort_order SMALLINT      NOT NULL DEFAULT 1 CHECK (sort_order BETWEEN 1 AND 10)
);

-- =============================================
-- NORM
-- Минимальная дистанция для разряда по возрастной
-- категории, полу и трамплину.
-- =============================================
CREATE TABLE norm (
                      id              BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      rank_id         BIGINT       NOT NULL REFERENCES sport_rank(id)   ON DELETE RESTRICT,
                      age_category_id BIGINT       NOT NULL REFERENCES age_category(id) ON DELETE RESTRICT,
                      gender          CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F')),
                      hill_id         BIGINT       NOT NULL REFERENCES hill(id)         ON DELETE RESTRICT,
                      min_distance    DECIMAL(5,2) NOT NULL CHECK (min_distance > 0),
                      CONSTRAINT uq_norm UNIQUE (rank_id, age_category_id, gender, hill_id)
);

-- =============================================
-- PERSON
-- Супертип. Хранит только атрибуты общие для всех физических лиц.
-- Разделение на подтипы ATHLETE/COACH устраняет частичные зависимости - 2НФ.
-- =============================================
CREATE TABLE person (
                        id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        first_name   VARCHAR(100) NOT NULL,
                        last_name    VARCHAR(100) NOT NULL,
                        middle_name  VARCHAR(100),
                        birth_date   DATE         NOT NULL CHECK (birth_date < CURRENT_DATE),
                        gender       CHAR(1)      CHECK (gender IN ('M', 'F')),
                        contact_info VARCHAR(200),
                        created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- PERSON_IMAGE
-- Фото человека хранится в БД (бэкап вместе с данными), связь 1:1 с PERSON.
-- =============================================
CREATE TABLE person_image (
                              person_id    BIGINT      PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
                              content_type VARCHAR(50) NOT NULL CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp')),
                              data         BYTEA       NOT NULL CHECK (OCTET_LENGTH(data) <= 2097152)
);

-- =============================================
-- COACH
-- Подтип PERSON. id одновременно PK и FK -
-- связь 1:1 с PERSON без дублирования персональных данных.
-- =============================================
CREATE TABLE coach (
                       id               BIGINT       PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
                       license_number   VARCHAR(50)  NOT NULL UNIQUE,
                       experience_years SMALLINT     NOT NULL DEFAULT 0 CHECK (experience_years >= 0)
);

-- =============================================
-- ATHLETE
-- Подтип PERSON. coach_id допускает NULL - спортсмен
-- может временно не иметь тренера (ON DELETE SET NULL).
-- number вынесен сюда, а не в PERSON, так как
-- атрибут специфичен только для спортсменов - 2НФ.
-- =============================================
CREATE TABLE athlete (
                         id             BIGINT      PRIMARY KEY REFERENCES person(id) ON DELETE CASCADE,
                         coach_id       BIGINT      REFERENCES coach(id) ON DELETE SET NULL,
                         number         VARCHAR(20) NOT NULL UNIQUE,
                         start_year     SMALLINT    NOT NULL,
                         current_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (current_status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
                         CONSTRAINT athlete_start_year_check CHECK (start_year >= 1900 AND start_year <= EXTRACT(YEAR FROM CURRENT_DATE))
);

-- =============================================
-- PHYSIOLOGICAL_RECORD
-- Историческая таблица замеров.
-- Каждый атрибут (height, weight, vo2_max, resting_hr)
-- зависит только от (athlete_id, measure_date) - 2НФ и 3НФ соблюдены.
-- Составной уникальный ключ исключает дублирование замеров в одну дату.
-- =============================================
CREATE TABLE physiological_record (
                                      id           BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                      athlete_id   BIGINT        NOT NULL REFERENCES athlete(id) ON DELETE CASCADE,
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
-- Трамплин вынесен в справочник HILL (hill_id).
-- =============================================
CREATE TABLE competition (
                             id               BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name             VARCHAR(200) NOT NULL,
                             date             DATE         NOT NULL,
                             location         VARCHAR(200),
                             hill_id          BIGINT       REFERENCES hill(id) ON DELETE RESTRICT,
                             level            VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
                                 CHECK (level IN ('LOCAL','REGIONAL','NATIONAL','INTERNATIONAL')),
                             max_participants SMALLINT     CHECK (max_participants > 0),
                             status           VARCHAR(20)  NOT NULL DEFAULT 'PLANNED'
                                 CONSTRAINT competition_status_check CHECK (status IN ('PLANNED', 'ACTIVE', 'FINISHED', 'CANCELLED'))
);

-- =============================================
-- JUMP_RESULT
-- Ассоциативная сущность между ATHLETE и COMPETITION.
-- Все атрибуты прыжка зависят только от id - BCNF.
-- ON DELETE RESTRICT защищает архивные данные от удаления.
-- Попытка уникальна для спортсмена в соревновании; время прыжка
-- и статус соревнования проверяет триггер trg_jump_result_validate.
-- =============================================
CREATE TABLE jump_result (
                             id             BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             athlete_id     BIGINT        NOT NULL REFERENCES athlete(id)     ON DELETE RESTRICT,
                             competition_id BIGINT        NOT NULL REFERENCES competition(id) ON DELETE RESTRICT,
                             timestamp      TIMESTAMP     NOT NULL,
                             distance       DECIMAL(5,2)  NOT NULL CHECK (distance > 0 AND distance <= 300),
                             style_points   DECIMAL(5,2)  CHECK (style_points >= 0 AND style_points <= 60),
                             wind_factor    DECIMAL(4,2)  CHECK (wind_factor BETWEEN -99.99 AND 99.99),
                             attempt_number SMALLINT      NOT NULL DEFAULT 1 CHECK (attempt_number >= 1),
                             gate_mark      VARCHAR(10),
                             CONSTRAINT uq_jump_attempt UNIQUE (athlete_id, competition_id, attempt_number)
);

-- =============================================
-- ATHLETE_RANK
-- История присвоения разрядов спортсменам (основание - прыжок).
-- =============================================
CREATE TABLE athlete_rank (
                              id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              athlete_id    BIGINT NOT NULL REFERENCES athlete(id)    ON DELETE RESTRICT,
                              rank_id       BIGINT NOT NULL REFERENCES sport_rank(id) ON DELETE RESTRICT,
                              assigned_date DATE   NOT NULL,
                              jump_id       BIGINT REFERENCES jump_result(id)         ON DELETE SET NULL,
                              CONSTRAINT uq_athlete_rank UNIQUE (athlete_id, rank_id)
);

-- =============================================
-- STANDARDS_MAP
-- Связующая таблица JUMP_RESULT - SPORT_RANK.
-- Составной PK (jump_id, rank_id) является суперключом -
-- BCNF соблюдена, транзитивных зависимостей нет.
-- Фиксирует факт выполнения норматива конкретным прыжком.
-- =============================================
CREATE TABLE standards_map (
                               jump_id BIGINT NOT NULL REFERENCES jump_result(id) ON DELETE CASCADE,
                               rank_id BIGINT NOT NULL REFERENCES sport_rank(id)  ON DELETE CASCADE,
                               PRIMARY KEY (jump_id, rank_id)
);

-- =============================================
-- RECORD
-- Фиксирует рекордные прыжки.
-- UNIQUE на jump_id гарантирует связь 1:1 с JUMP_RESULT.
-- Атрибуты type и verification_date зависят
-- только от id - 3НФ и BCNF соблюдены.
-- =============================================
CREATE TABLE record (
                        id                BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        jump_id           BIGINT      NOT NULL UNIQUE REFERENCES jump_result(id) ON DELETE CASCADE,
                        type              VARCHAR(30) NOT NULL CONSTRAINT record_type_check CHECK (type IN ('CLUB', 'REGIONAL', 'NATIONAL', 'INTERNATIONAL')),
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
CREATE INDEX idx_competition_date        ON competition(date);
CREATE INDEX idx_competition_hill        ON competition(hill_id);
CREATE INDEX idx_standards_rank          ON standards_map(rank_id);
CREATE INDEX idx_sport_rank_order        ON sport_rank(sort_order);
CREATE INDEX idx_norm_lookup             ON norm(age_category_id, gender, hill_id);
CREATE INDEX idx_athlete_rank_athlete    ON athlete_rank(athlete_id, assigned_date);
