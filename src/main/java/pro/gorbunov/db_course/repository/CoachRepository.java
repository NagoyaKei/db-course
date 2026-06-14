package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.gorbunov.db_course.model.Coach;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CoachRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Coach> rowMapper = (rs, rowNum) -> {
        Coach c = new Coach();
        c.setPersonId(rs.getLong("person_id"));
        c.setLastName(rs.getString("last_name"));
        c.setFirstName(rs.getString("first_name"));
        c.setMiddleName(rs.getString("middle_name"));
        c.setBirthDate(rs.getDate("birth_date").toLocalDate());
        c.setGender(rs.getString("gender"));
        c.setContactInfo(rs.getString("contact_info"));
        c.setLicenseNumber(rs.getString("license_number"));
        c.setSpecialization(rs.getString("specialization"));
        c.setExperienceYears(rs.getInt("experience_years"));
        return c;
    };

    private static final String BASE_SELECT = """
            SELECT
                p.person_id, p.last_name, p.first_name, p.middle_name,
                p.birth_date, p.gender, p.contact_info,
                c.license_number, c.specialization, c.experience_years
            FROM coach c
            JOIN person p ON p.person_id = c.person_id
            """;

    public List<Coach> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY p.last_name", rowMapper);
    }

    public Optional<Coach> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE c.person_id = ?", rowMapper, id)
                .stream().findFirst();
    }

    @Transactional
    public Long create(Coach coach) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO person (last_name, first_name, middle_name, birth_date, gender, contact_info)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, coach.getLastName());
            ps.setString(2, coach.getFirstName());
            ps.setString(3, coach.getMiddleName());
            ps.setDate(4, java.sql.Date.valueOf(coach.getBirthDate()));
            ps.setString(5, coach.getGender());
            ps.setString(6, coach.getContactInfo());
            return ps;
        }, keyHolder);

        Long personId = keyHolder.getKey().longValue();

        jdbc.update(
                """
                INSERT INTO coach (person_id, license_number, specialization, experience_years)
                VALUES (?, ?, ?, ?)
                """,
                personId,
                coach.getLicenseNumber(),
                coach.getSpecialization(),
                coach.getExperienceYears() != null ? coach.getExperienceYears() : 0
        );

        return personId;
    }

    @Transactional
    public void update(Coach coach) {
        jdbc.update(
                """
                UPDATE person SET
                    last_name = ?, first_name = ?, middle_name = ?,
                    birth_date = ?, gender = ?, contact_info = ?
                WHERE person_id = ?
                """,
                coach.getLastName(), coach.getFirstName(), coach.getMiddleName(),
                java.sql.Date.valueOf(coach.getBirthDate()),
                coach.getGender(), coach.getContactInfo(), coach.getPersonId()
        );

        jdbc.update(
                """
                UPDATE coach SET
                    license_number = ?, specialization = ?, experience_years = ?
                WHERE person_id = ?
                """,
                coach.getLicenseNumber(), coach.getSpecialization(),
                coach.getExperienceYears(), coach.getPersonId()
        );
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM coach", Integer.class);
    }
}