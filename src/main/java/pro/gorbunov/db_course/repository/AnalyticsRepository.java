package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.AthleteRef;
import pro.gorbunov.db_course.model.CorrelationRow;
import pro.gorbunov.db_course.model.RankCount;

@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private final JdbcTemplate jdbc;

    public List<AthleteRef> findAthletesWithJumps() {
        return jdbc.query(
                """
                SELECT DISTINCT jc.athlete_id, jc.athlete_name, cr.rank_name
                FROM v_jump_context jc
                LEFT JOIN v_athlete_current_rank cr ON cr.athlete_id = jc.athlete_id
                ORDER BY jc.athlete_name
                """,
                new DataClassRowMapper<>(AthleteRef.class)
        );
    }

    public List<CorrelationRow> findCorrelations() {
        return jdbc.query(
                """
                SELECT hill_type, jumps_count, height_correlation, weight_correlation
                FROM v_physio_distance_correlation
                ORDER BY hill_type
                """,
                new DataClassRowMapper<>(CorrelationRow.class)
        );
    }

    public List<RankCount> findRankDistribution() {
        return jdbc.query(
                """
                SELECT COALESCE(sr.name, 'Без разряда') AS rank_name, COUNT(*) AS athletes
                FROM athlete a
                LEFT JOIN v_athlete_current_rank cr ON cr.athlete_id = a.id
                LEFT JOIN sport_rank sr ON sr.id = cr.rank_id
                GROUP BY sr.name, sr.sort_order
                ORDER BY COALESCE(sr.sort_order, 0)
                """,
                new DataClassRowMapper<>(RankCount.class)
        );
    }
}
