package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.AgeRecord;

@Repository
@RequiredArgsConstructor
public class AgeRecordRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<AgeRecord> rowMapper = (rs, rowNum) -> new AgeRecord(
            rs.getString("age_category_type"),
            rs.getString("age_category_name"),
            rs.getString("gender"),
            rs.getString("hill_type"),
            rs.getString("hill_name"),
            rs.getLong("athlete_id"),
            rs.getString("athlete_name"),
            rs.getBigDecimal("distance"),
            rs.getDate("jump_date").toLocalDate(),
            rs.getString("competition_name"),
            rs.getString("record_type")
    );

    public List<AgeRecord> findByHill(String hillType) {
        return jdbc.query(
                """
                SELECT age_category_type, age_category_name, gender, hill_type, hill_name, athlete_id, athlete_name,
                       distance, jump_date, competition_name, record_type
                FROM v_age_records
                WHERE hill_type = ?
                ORDER BY age_category_id, gender
                """,
                rowMapper, hillType
        );
    }
}
