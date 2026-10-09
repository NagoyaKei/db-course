package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.ProtocolRow;

@Repository
@RequiredArgsConstructor
public class ProtocolRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<ProtocolRow> rowMapper = new DataClassRowMapper<>(ProtocolRow.class);

    public List<ProtocolRow> findByCompetition(Long competitionId) {
        return jdbc.query(
                """
                SELECT athlete_id, athlete_name, jumps_count, best_distance, total_points, place
                FROM v_competition_protocol
                WHERE competition_id = ?
                ORDER BY place, athlete_name
                """,
                rowMapper, competitionId
        );
    }

    public void register(Long competitionId, String resultsJson) {
        jdbc.update("CALL p_register_competition_results(?, ?::jsonb)", competitionId, resultsJson);
    }
}
