# Таблицы базы данных

Схема: `sql/01_init.sql` (единый файл, правки схемы вносятся прямо в него). Справочники: `sql/02_reference_data.sql`, тестовые данные: `sql/03_test_data.sql` (20 человек, 16 спортсменов, 15 соревнований 2023-2026, около 240 прыжков, 165 замеров; новые данные генерируются SQL без `random()`, повторный запуск дает те же значения), фото людей: `sql/04_person_images.sql` (выгрузка из рабочей БД в base64, при смене фото пересоздавать файл), объекты БД: `sql/05_db_objects.sql`. Файлы выполняются по порядку номеров. При изменении схемы обновлять этот файл на месте.

Соглашения:
- Колонки без префикса имени таблицы: PK везде `id`, а не `rank_id`, `person_id` и т.п. Внешние ключи называются по роли: `athlete_id`, `rank_id`, `hill_id`.
- Справочники: `type` - код латиницей в UPPER_CASE (константа для кода), `name` - название на русском.
- Константы в CHECK (`status`, `level`, `type`, `current_status`, `gender`) тоже латиницей в UPPER_CASE; в Java им соответствуют enum в `model` (`CompetitionStatus`, `CompetitionLevel`, `AthleteStatus`, `RecordType`, `Gender`), совпадение проверяет тест `EnumsMatchDatabaseTest`.

## Справочники

### age_category
Возрастные категории (U14, U18, U21, OPEN). Диапазоны возраста включительно и не пересекаются.
- `id` PK, `type` NOT NULL UNIQUE, `name` NOT NULL, `min_age` NOT NULL, `max_age` (NULL = без верхней границы)
- `age_range` - сгенерированный `int4range`, по нему `EXCLUDE` запрещает пересечения

### hill
Трамплины. Дистанции на разных трамплинах несравнимы.
- `id` PK, `type` NOT NULL UNIQUE (например `K-90`), `name` NOT NULL, `k_point`, `meter_value` (очки за метр)

### sport_rank
Спортивные разряды (коды THIRD_CLASS, SECOND_CLASS, FIRST_CLASS, CMS, MS). Требования к дистанции живут в `norm`.
- `id` PK, `type` NOT NULL UNIQUE, `name` NOT NULL, `sort_order` 1..10 (чем больше, тем выше)

## Люди

### person
Супертип: общие данные любого человека.
- `id` PK, `first_name`, `last_name`, `middle_name`, `birth_date` NOT NULL, `gender` (M/F), `contact_info`, `created_at`

### person_image
Фото человека (спортсмена или тренера), не больше одного на person. Хранится в БД (BYTEA), чтобы попадать в бэкап вместе с данными и не зависеть от файлов контейнера.
- `person_id` PK и FK person (CASCADE), `content_type` (image/jpeg, image/png, image/webp), `data` (до 2 МБ)
- Загрузка: формы спортсмена и тренера; отдача: `GET /persons/{id}/photo` (`PersonPhotoController`). Тип файла проверяется по сигнатуре байтов, SVG не допускается

### coach
Подтип person (1:1, `id` PK и FK person, CASCADE).
- `license_number` NOT NULL UNIQUE, `experience_years`

### athlete
Подтип person (1:1, `id` PK и FK person, CASCADE).
- `coach_id` FK coach (SET NULL), `number` NOT NULL UNIQUE, `start_year`, `current_status` (ACTIVE/INACTIVE/SUSPENDED)

## Соревнования и результаты

### competition
Соревнование.
- `id` PK, `name`, `date`, `location`, `hill_id` FK hill (RESTRICT, может быть NULL), `level` (LOCAL/REGIONAL/NATIONAL/INTERNATIONAL), `max_participants`, `status` (PLANNED/ACTIVE/FINISHED/CANCELLED, по умолчанию PLANNED)

### jump_result
Один прыжок спортсмена на соревновании.
- `id` PK, `athlete_id` FK athlete (RESTRICT), `competition_id` FK competition (RESTRICT), `timestamp`, `distance`, `style_points`, `wind_factor`, `attempt_number` (NOT NULL, по умолчанию 1), `gate_mark`
- `UNIQUE (athlete_id, competition_id, attempt_number)`; прыжок допустим только в соревновании ACTIVE или FINISHED и в день соревнования (триггер `trg_jump_result_validate`, пустой `timestamp` заполняется датой соревнования и текущим временем, значения по умолчанию у `timestamp` нет)

### record
Рекордный прыжок (1:1 с jump_result через UNIQUE `jump_id`).
- `id` PK, `jump_id` FK jump_result (CASCADE), `type` (CLUB/REGIONAL/NATIONAL/INTERNATIONAL), `verification_date`

## Физиология

### physiological_record
История замеров спортсмена. Уникальна пара (`athlete_id`, `measure_date`).
- `id` PK, `athlete_id` FK athlete (CASCADE), `measure_date`, `height`, `weight`, `vo2_max`, `resting_hr`, `notes`

## Нормативы и разряды

### norm
Минимальная дистанция для разряда в зависимости от возрастной категории, пола и трамплина. Уникальна четверка (`rank_id`, `age_category_id`, `gender`, `hill_id`).
- `id` PK, `rank_id` FK sport_rank, `age_category_id` FK age_category, `gender` (M/F), `hill_id` FK hill, `min_distance` (все FK RESTRICT)

### standards_map
Факт выполнения норматива конкретным прыжком. PK (`jump_id`, `rank_id`), оба FK CASCADE.

### athlete_rank
История присвоения разрядов. Уникальна пара (`athlete_id`, `rank_id`).
- `id` PK, `athlete_id` FK athlete (RESTRICT), `rank_id` FK sport_rank (RESTRICT), `assigned_date`, `jump_id` FK jump_result (SET NULL, прыжок-основание)

## Связи (кратко)
- person 1:1 coach, person 1:1 athlete; coach 1:N athlete
- athlete 1:N jump_result, physiological_record, athlete_rank
- competition 1:N jump_result; hill 1:N competition, norm; person 1:1 person_image
- jump_result 1:1 record; jump_result M:N sport_rank через standards_map
- sport_rank M:N age_category, hill через norm

## Объекты БД (`sql/05_db_objects.sql`)

Выполняется после данных. В конце файла пересчитываются `standards_map` и `athlete_rank` для существующих прыжков, поэтому в тестовых данных их вставок нет.

Представления:
- `v_jump_context` - прыжок с возрастом и категорией спортсмена на дату прыжка, трамплином и очками. Очки = 60 + (дистанция - K-точка) * очки за метр + баллы за стиль + поправка на ветер (допущение, не из задания). Основа для остальных представлений.
- `v_age_records` - лучший результат по каждой возрастной категории, полу и трамплину, с типом рекорда из `record`, если он зафиксирован.
- `v_norms` - читаемая таблица нормативов (категория, пол, трамплин, разряд, дистанция).
- `v_jump_dynamics` - динамика спортсмена на трамплине: предыдущий прыжок, разница, лучший на данный момент, скользящее среднее за 3 прыжка, рост и вес по ближайшему замеру до прыжка.
- `v_physio_distance_correlation` - корреляция роста и веса с дистанцией по трамплинам.
- `v_competition_protocol` - протокол соревнования: число прыжков, лучшая дистанция, сумма очков, место.
- `v_athlete_current_rank` - высший присвоенный разряд спортсмена.
- `v_norm_fulfilment` - первое выполнение каждого норматива спортсменом: дата прыжка, дистанция, замер (рост, вес, VO2, пульс) последний на эту дату или раньше.

Процедуры и функции:
- `p_assign_rank(p_jump_id)` - пересчитывает `standards_map` для прыжка (какие нормативы выполнены) и присваивает спортсмену высший из них, если он выше текущего разряда. Разряд не отзывается при пересчете.
- `p_calculate_forecast(p_athlete_id, p_horizon_days, OUT forecast_distance, OUT trend_per_year, OUT jumps_used, OUT r_squared)` - линейная регрессия дистанции относительно K-точки по времени, прогноз пересчитывается в метры на последнем трамплине спортсмена. Нужно минимум два прыжка в разные даты, иначе результат пустой.
- `p_register_competition_results(p_competition_id, p_results JSONB)` (время прыжка заполняет триггер) - добавляет все прыжки соревнования одним вызовом (все или ни одного); соревнование должно быть в статусе ACTIVE или FINISHED.
- `fn_age_on(p_birth_date, p_on)` - полных лет на дату.

Триггеры:
- `trg_validate_physiological_record` - дата замера не раньше даты рождения.
- `trg_validate_athlete_start_year` - год начала занятий не раньше года рождения.
- `trg_jump_result_assign_rank` - после вставки или изменения дистанции прыжка вызывает `p_assign_rank`.
- `trg_jump_result_validate` - статус соревнования ACTIVE или FINISHED, дата прыжка равна дате соревнования, пустое время заполняется.
