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
import pro.gorbunov.db_course.model.Athlete;
import pro.gorbunov.db_course.model.AthleteStatus;

@Repository
@RequiredArgsConstructor
public class AthleteRepository {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final PersonRepository personRepository;

    private final RowMapper<Athlete> rowMapper = new BeanPropertyRowMapper<>(Athlete.class);

    private static final String BASE_SELECT = """
            SELECT
                p.id AS person_id, p.last_name, p.first_name, p.middle_name,
                p.birth_date, p.gender, p.contact_info,
                EXISTS (SELECT 1 FROM person_image pi WHERE pi.person_id = p.id) AS has_photo,
                a.coach_id, a.number AS athlete_number, a.start_year, a.current_status,
                cp.last_name || ' ' || cp.first_name AS coach_full_name,
                cr.rank_name
            FROM athlete a
            JOIN person p  ON p.id = a.id
            LEFT JOIN coach c  ON c.id = a.coach_id
            LEFT JOIN person cp ON cp.id = c.id
            LEFT JOIN v_athlete_current_rank cr ON cr.athlete_id = a.id
            """;

    public List<Athlete> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY p.last_name", rowMapper);
    }

    public Optional<Athlete> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE a.id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public List<Athlete> findByStatus(AthleteStatus status) {
        return jdbc.query(BASE_SELECT + "WHERE a.current_status = ? ORDER BY p.last_name",
                rowMapper, status.name());
    }

    public List<Athlete> findByCoach(Long coachId) {
        return jdbc.query(BASE_SELECT + "WHERE a.coach_id = ? ORDER BY p.last_name",
                rowMapper, coachId);
    }

    @Transactional
    public Long create(Athlete athlete) {
        Long personId = personRepository.create(athlete);
        athlete.setPersonId(personId);
        namedJdbc.update(
                """
                INSERT INTO athlete (id, coach_id, number, start_year, current_status)
                VALUES (:personId, :coachId, :athleteNumber, :startYear, :currentStatus)
                """,
                SqlParams.of(athlete)
        );
        return personId;
    }

    @Transactional
    public void update(Athlete athlete) {
        personRepository.update(athlete);
        namedJdbc.update(
                """
                UPDATE athlete SET coach_id = :coachId, number = :athleteNumber, start_year = :startYear, current_status = :currentStatus
                WHERE id = :personId
                """,
                SqlParams.of(athlete)
        );
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM athlete", Integer.class);
    }
}