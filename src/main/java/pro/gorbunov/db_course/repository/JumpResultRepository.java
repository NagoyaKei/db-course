package pro.gorbunov.db_course.repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.JumpResult;

@Repository
@RequiredArgsConstructor
public class JumpResultRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<JumpResult> rowMapper = (rs, rowNum) -> {
        JumpResult j = new JumpResult();
        j.setJumpId(rs.getLong("jump_id"));
        j.setAthleteId(rs.getLong("athlete_id"));
        j.setCompetitionId(rs.getLong("competition_id"));
        j.setDistance(rs.getBigDecimal("distance"));
        j.setStylePoints(rs.getBigDecimal("style_points"));
        j.setWindFactor(rs.getBigDecimal("wind_factor"));
        j.setGateMark(rs.getString("gate_mark"));
        Timestamp ts = rs.getTimestamp("timestamp");
        j.setJumpTimestamp(ts != null ? ts.toLocalDateTime() : null);
        j.setAttemptNumber(rs.getInt("attempt_number"));
        j.setAthleteFullName(rs.getString("athlete_full_name"));
        j.setCompetitionName(rs.getString("competition_name"));
        j.setHillType(rs.getString("hill_type"));
        j.setAgeCategoryName(rs.getString("age_category_name"));
        j.setPoints(rs.getBigDecimal("points"));
        return j;
    };

    private static final String BASE_SELECT = """
            SELECT
                jr.id AS jump_id, jr.athlete_id, jr.competition_id,
                jr.distance, jr.style_points, jr.wind_factor,
                jr.gate_mark, jr.timestamp, jr.attempt_number,
                p.last_name || ' ' || p.first_name as athlete_full_name,
                c.name as competition_name,
                jc.hill_type, jc.age_category_name, jc.points
            FROM jump_result jr
            JOIN athlete a    ON a.id = jr.athlete_id
            JOIN person p     ON p.id = a.id
            JOIN competition c ON c.id  = jr.competition_id
            JOIN v_jump_context jc ON jc.jump_id = jr.id
            """;

    public List<JumpResult> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY jr.timestamp DESC", rowMapper);
    }

    public Optional<JumpResult> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE jr.id = ?", rowMapper, id)
                .stream().findFirst();
    }

    public List<JumpResult> findByAthlete(Long athleteId) {
        return jdbc.query(
                BASE_SELECT + "WHERE jr.athlete_id = ? ORDER BY jr.timestamp DESC",
                rowMapper, athleteId
        );
    }

    public List<JumpResult> findByCompetition(Long compId) {
        return jdbc.query(
                BASE_SELECT + "WHERE jr.competition_id = ? ORDER BY jr.distance DESC",
                rowMapper, compId
        );
    }

    public Long create(JumpResult jump) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO jump_result
                        (athlete_id, competition_id, timestamp, distance, style_points, wind_factor, gate_mark)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[]{"id"}
            );
            ps.setLong(1, jump.getAthleteId());
            ps.setLong(2, jump.getCompetitionId());
            ps.setTimestamp(3, Timestamp.valueOf(
                    jump.getJumpTimestamp() != null ? jump.getJumpTimestamp() : LocalDateTime.now()
            ));
            ps.setBigDecimal(4, jump.getDistance());
            ps.setBigDecimal(5, jump.getStylePoints());
            ps.setBigDecimal(6, jump.getWindFactor());
            ps.setString(7, jump.getGateMark());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void update(JumpResult jump) {
        jdbc.update(
                """
                UPDATE jump_result SET
                    distance = ?, style_points = ?, wind_factor = ?, gate_mark = ?
                WHERE id = ?
                """,
                jump.getDistance(), jump.getStylePoints(),
                jump.getWindFactor(), jump.getGateMark(),
                jump.getJumpId()
        );
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM jump_result WHERE id = ?", id);
    }
}