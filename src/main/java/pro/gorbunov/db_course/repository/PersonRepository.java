package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Person;

@Repository
@RequiredArgsConstructor
public class PersonRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public Long create(Person person) {
        return jdbc.queryForObject(
                """
                INSERT INTO person (last_name, first_name, middle_name, birth_date, gender, contact_info)
                VALUES (:lastName, :firstName, :middleName, :birthDate, :gender, :contactInfo)
                RETURNING id
                """,
                SqlParams.of(person), Long.class
        );
    }

    public void update(Person person) {
        jdbc.update(
                """
                UPDATE person SET last_name = :lastName, first_name = :firstName, middle_name = :middleName,
                    birth_date = :birthDate, gender = :gender, contact_info = :contactInfo
                WHERE id = :personId
                """,
                SqlParams.of(person)
        );
    }
}
