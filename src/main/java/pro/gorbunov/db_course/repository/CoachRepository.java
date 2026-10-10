package pro.gorbunov.db_course.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.gorbunov.db_course.model.Coach;

@Repository
@RequiredArgsConstructor
public class CoachRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final PersonRepository personRepository;

    private final RowMapper<Coach> rowMapper = new BeanPropertyRowMapper<>(Coach.class);

    private static final String BASE_SELECT = """
            SELECT
                p.id AS person_id, p.last_name, p.first_name, p.middle_name,
                p.birth_date, p.gender, p.contact_info,
                EXISTS (SELECT 1 FROM person_image pi WHERE pi.person_id = p.id) AS has_photo,
                c.license_number, c.experience_years,
                a.id AS athlete_id, cr.rank_name
            FROM coach c
            JOIN person p ON p.id = c.id
            LEFT JOIN athlete a ON a.id = c.id
            LEFT JOIN v_athlete_current_rank cr ON cr.athlete_id = c.id
            """;

    public List<Coach> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY p.last_name", rowMapper);
    }

    public Optional<Coach> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE c.id = ?", rowMapper, id)
                .stream().findFirst();
    }

    @Transactional
    public Long create(Coach coach) {
        Long personId = personRepository.create(coach);
        coach.setPersonId(personId);
        namedJdbc.update(
                """
                INSERT INTO coach (id, license_number, experience_years)
                VALUES (:personId, :licenseNumber, :experienceYears)
                """,
                SqlParams.of(coach)
        );
        return personId;
    }

    @Transactional
    public void update(Coach coach) {
        personRepository.update(coach);
        namedJdbc.update(
                """
                UPDATE coach SET license_number = :licenseNumber, experience_years = :experienceYears
                WHERE id = :personId
                """,
                SqlParams.of(coach)
        );
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM coach", Integer.class);
    }
}