package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.gorbunov.db_course.model.Athlete;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AthleteRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Athlete> rowMapper = (rs, rowNum) -> {
        Athlete a = new Athlete();
        a.setPersonId(rs.getLong("person_id"));
        a.setLastName(rs.getString("last_name"));
        a.setFirstName(rs.getString("first_name"));
        a.setMiddleName(rs.getString("middle_name"));
        a.setBirthDate(rs.getDate("birth_date").toLocalDate());
        a.setGender(rs.getString("gender"));
        a.setContactInfo(rs.getString("contact_info"));
        a.setCoachId(rs.getLong("coach_id"));
        a.setAthleteNumber(rs.getString("athlete_number"));
        a.setStartYear(rs.getInt("start_year"));
        a.setCurrentStatus(rs.getString("current_status"));
        a.setCoachFullName(rs.getString("coach_full_name"));
        return a;
    };

    private static final String BASE_SELECT = """
            SELECT
                p.person_id, p.last_name, p.first_name, p.middle_name,
                p.birth_date, p.gender, p.contact_info,
                a.coach_id, a.athlete_number, a.start_year, a.current_status,
                cp.last_name || ' ' || cp.first_name AS coach_full_name
            FROM athlete a
            JOIN person p  ON p.person_id = a.person_id
            LEFT JOIN coach c  ON c.person_id = a.coach_id
            LEFT JOIN person cp ON cp.person_id = c.person_id
            """;

    public List<Athlete> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY p.last_name", rowMapper);
    }

    public Optional<Athlete> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE a.person_id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public List<Athlete> findByStatus(String status) {
        return jdbc.query(BASE_SELECT + "WHERE a.current_status = ? ORDER BY p.last_name",
                rowMapper, status);
    }

    public List<Athlete> findByCoach(Long coachId) {
        return jdbc.query(BASE_SELECT + "WHERE a.coach_id = ? ORDER BY p.last_name",
                rowMapper, coachId);
    }

    @Transactional
    public Long create(Athlete athlete) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO person (last_name, first_name, middle_name, birth_date, gender, contact_info)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, athlete.getLastName());
            ps.setString(2, athlete.getFirstName());
            ps.setString(3, athlete.getMiddleName());
            ps.setDate(4, java.sql.Date.valueOf(athlete.getBirthDate()));
            ps.setString(5, athlete.getGender());
            ps.setString(6, athlete.getContactInfo());
            return ps;
        }, keyHolder);

        Long personId = keyHolder.getKey().longValue();

        jdbc.update(
                """
                INSERT INTO athlete (person_id, coach_id, athlete_number, start_year, current_status)
                VALUES (?, ?, ?, ?, ?)
                """,
                personId,
                athlete.getCoachId(),
                athlete.getAthleteNumber(),
                athlete.getStartYear(),
                athlete.getCurrentStatus() != null ? athlete.getCurrentStatus() : "active"
        );

        return personId;
    }

    @Transactional
    public void update(Athlete athlete) {
        jdbc.update(
                """
                UPDATE person SET
                    last_name = ?, first_name = ?, middle_name = ?,
                    birth_date = ?, gender = ?, contact_info = ?
                WHERE person_id = ?
                """,
                athlete.getLastName(), athlete.getFirstName(), athlete.getMiddleName(),
                java.sql.Date.valueOf(athlete.getBirthDate()),
                athlete.getGender(), athlete.getContactInfo(), athlete.getPersonId()
        );

        jdbc.update(
                """
                UPDATE athlete SET
                    coach_id = ?, athlete_number = ?, start_year = ?, current_status = ?
                WHERE person_id = ?
                """,
                athlete.getCoachId(), athlete.getAthleteNumber(),
                athlete.getStartYear(), athlete.getCurrentStatus(),
                athlete.getPersonId()
        );
    }

    public void deactivate(Long id) {
        jdbc.update("UPDATE athlete SET current_status = 'retired' WHERE person_id = ?", id);
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM athlete", Integer.class);
    }
}