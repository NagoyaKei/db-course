# Соглашения по Java-коду

Цель - минимум шаблонного кода.

## Lombok
- Внедрение зависимостей только через `@RequiredArgsConstructor` и `private final` поля, ручных конструкторов нет.
- Модели с `@Data` без `@NoArgsConstructor` (он уже есть). Утилитные классы - `@UtilityClass`.
- Версия Lombok задается Spring Boot (в `pom.xml` без версии, `optional`).

## Доступ к данным
- Простые таблицы (`hill`, `sport_rank`, `physiological_record`, `record`) - интерфейс `extends ListCrudRepository<Сущность, Long>` с производными методами.
- Таблицы с данными из соединений (`competition`, `jump_result`) - интерфейс `extends Repository<...>` только с нужными методами: `save`, `deleteById`, `count` и запросы `@Query`; поля из соединений помечены `@ReadOnlyProperty`. Унаследованные `findAll` и `findById` не использовать: они обратятся к несуществующим колонкам.
- Сущности Spring Data: `@Id`, `@Column("...")` только там, где имя колонки не совпадает с полем (`date`, `timestamp`, `k_point`, `id` для `recordId` и `jumpId`). Значения по умолчанию (`LocalDate.now()`, enum) задаются инициализаторами полей, так как Spring Data пишет `NULL` вместо значения по умолчанию из БД.
- Ошибки БД при `save` и `deleteById` приходят как `DbActionExecutionException`: причина `DuplicateKeyException` для уникальных ключей (пример - `HillController.save`).
- Спортсмены, тренеры, `person`, фото, нормы и рекорды по возрастам - `JdbcTemplate`: чтение `BeanPropertyRowMapper` (классы) или `DataClassRowMapper` (records), имена колонок в SQL должны совпадать с полями (`person_id`, `athlete_number`), запись именованными параметрами `NamedParameterJdbcTemplate` и `SqlParams.of(bean)` (превращает enum в строку, `JdbcTemplate` сам enum не пишет). Новые строки получают id через `INSERT ... RETURNING id`.

## Ошибки и валидация
- Не найдена запись: `findById(id).orElseThrow(NotFoundException::new)`, ответ 404. Отдельного обработчика ошибок нет, страницу ошибки (`error.html`) рисует Spring Boot по коду ответа (400 при неверном значении enum в адресе, 404, 500).
- Валидация только аннотациями `jakarta.validation` в моделях и `@Valid` в контроллере. Ограничения должны совпадать со схемой и данными: это проверяет `SeedDataPassesValidationTest`.

## Тесты (нужна БД, адрес в переменной `SPRING_DATASOURCE_URL`)
- `RepositoriesTest` - создание, чтение, правка, удаление по всем репозиториям (откат транзакции, но счетчики идентификаторов растут, поэтому для запуска лучше временная БД).
- `SeedDataPassesValidationTest` - все сущности из тестовых данных проходят валидацию.
- `EnumsMatchDatabaseTest` - значения enum совпадают с `CHECK` в БД.
- `DbCourseApplicationTests` - контекст поднимается.
