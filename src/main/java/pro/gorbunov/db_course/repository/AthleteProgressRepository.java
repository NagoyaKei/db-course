package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Forecast;
import pro.gorbunov.db_course.model.JumpDynamics;
import pro.gorbunov.db_course.model.NormFulfilment;

@Repository
@RequiredArgsConstructor
public class AthleteProgressRepository {

    private final JdbcTemplate jdbc;

    public List<JumpDynamics> findDynamicsOnLastHill(Long athleteId) {
        return jdbc.query(
                """
                SELECT jump_date, hill_type, distance, moving_avg_3 AS moving_average
                FROM v_jump_dynamics
                WHERE athlete_id = ?
                  AND hill_id = (SELECT hill_id FROM v_jump_dynamics WHERE athlete_id = ? ORDER BY jump_date DESC, jump_id DESC LIMIT 1)
                ORDER BY jump_date, jump_id
                """,
                new DataClassRowMapper<>(JumpDynamics.class), athleteId, athleteId
        );
    }

    public List<NormFulfilment> findNormFulfilment(Long athleteId) {
        return jdbc.query(
                """
                SELECT jump_date, rank_name, hill_type, distance, measure_date, height, weight
                FROM v_norm_fulfilment
                WHERE athlete_id = ?
                ORDER BY jump_date, rank_sort_order
                """,
                new DataClassRowMapper<>(NormFulfilment.class), athleteId
        );
    }

    public Forecast forecast(Long athleteId, int horizonDays) {
        return jdbc.queryForObject(
                "CALL p_calculate_forecast(?, ?, NULL, NULL, NULL, NULL)",
                new DataClassRowMapper<>(Forecast.class), athleteId, horizonDays
        );
    }
}
