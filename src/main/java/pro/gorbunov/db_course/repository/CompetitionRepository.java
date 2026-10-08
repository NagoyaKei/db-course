package pro.gorbunov.db_course.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Competition;

@Repository
@RequiredArgsConstructor
public class CompetitionRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Competition> rowMapper = (rs, rowNum) -> {
        Competition c = new Competition();
        c.setId(rs.getLong("id"));
        c.setName(rs.getString("name"));
        c.setLocation(rs.getString("location"));
        c.setCompetitionDate(rs.getDate("date").toLocalDate());
        c.setHillId(rs.getObject("hill_id", Long.class));
        c.setHillName(rs.getString("hill_name"));
        c.setLevel(rs.getString("level"));
        c.setMaxParticipants(rs.getInt("max_participants"));
        c.setStatus(rs.getString("status"));
        c.setParticipantsCount(rs.getInt("participants_count"));
        return c;
    };

    private static final String BASE_SELECT = """
            SELECT
                c.id, c.name, c.location, c.date,
                c.hill_id, h.name AS hill_name, c.level, c.max_participants, c.status,
                COUNT(DISTINCT jr.athlete_id) AS participants_count
            FROM competition c
            LEFT JOIN hill h ON h.id = c.hill_id
            LEFT JOIN jump_result jr ON jr.competition_id = c.id
            GROUP BY c.id, c.date, h.id
            """;

    public List<Competition> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY c.date DESC", rowMapper);
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
                    INSERT INTO competition (name, location, date, hill_id, level, max_participants, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, competition.getName());
            ps.setString(2, competition.getLocation());
            ps.setDate(3, java.sql.Date.valueOf(competition.getCompetitionDate()));
            ps.setObject(4, competition.getHillId());
            ps.setString(5, competition.getLevel() != null ? competition.getLevel() : "LOCAL");
            ps.setInt(6, competition.getMaxParticipants() != null ? competition.getMaxParticipants() : 0);
            ps.setString(7, competition.getStatus() != null ? competition.getStatus() : "PLANNED");
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void update(Competition competition) {
        jdbc.update(
                """
                UPDATE competition SET
                    name = ?, location = ?, date = ?,
                    hill_id = ?, level = ?, max_participants = ?, status = ?
                WHERE id = ?
                """,
                competition.getName(), competition.getLocation(),
                java.sql.Date.valueOf(competition.getCompetitionDate()),
                competition.getHillId(), competition.getLevel(),
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