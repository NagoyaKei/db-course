package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Competition;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompetitionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Competition> rowMapper = (rs, rowNum) -> {
        Competition c = new Competition();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setLocation(rs.getString("location"));
        c.setCompetitionDate(rs.getDate("competition_date").toLocalDate());
        c.setHillSize(rs.getString("hill_size"));
        c.setLevel(rs.getString("level"));
        c.setMaxParticipants(rs.getInt("max_participants"));
        c.setStatus(rs.getString("status"));
        c.setParticipantsCount(rs.getInt("participants_count"));
        return c;
    };

    private static final String BASE_SELECT = """
            SELECT
                c.id, c.name, c.location, c.competition_date,
                c.hill_size, c.level, c.max_participants, c.status,
                COUNT(DISTINCT jr.athlete_id) AS participants_count
            FROM competition c
            LEFT JOIN jump_result jr ON jr.competition_id = c.id
            GROUP BY c.id, c.competition_date
            """;

    public List<Competition> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY c.competition_date DESC", rowMapper);
    }

    public Optional<Competition> findById(Long id) {
        return jdbc.query(
                BASE_SELECT + "HAVING c.id = ?",
                rowMapper, id
        ).stream().findFirst();
    }

    public Long create(Competition competition) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO competition (name, location, competition_date, hill_size, level, max_participants, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, competition.getName());
            ps.setString(2, competition.getLocation());
            ps.setDate(3, java.sql.Date.valueOf(competition.getCompetitionDate()));
            ps.setString(4, competition.getHillSize());
            ps.setString(5, competition.getLevel() != null ? competition.getLevel() : "local");
            ps.setInt(6, competition.getMaxParticipants() != null ? competition.getMaxParticipants() : 0);
            ps.setString(7, competition.getStatus() != null ? competition.getStatus() : "planned");
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void update(Competition competition) {
        jdbc.update(
                """
                UPDATE competition SET
                    name = ?, location = ?, competition_date = ?,
                    hill_size = ?, level = ?, max_participants = ?, status = ?
                WHERE id = ?
                """,
                competition.getName(), competition.getLocation(),
                java.sql.Date.valueOf(competition.getCompetitionDate()),
                competition.getHillSize(), competition.getLevel(),
                competition.getMaxParticipants(), competition.getStatus(),
                competition.getId()
        );
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM competition WHERE id = ?", id);
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM competition", Integer.class);
    }
}