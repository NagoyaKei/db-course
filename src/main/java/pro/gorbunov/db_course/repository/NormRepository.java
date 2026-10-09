package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.NormRow;

@Repository
@RequiredArgsConstructor
public class NormRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<NormRow> rowMapper = (rs, rowNum) -> new NormRow(
            rs.getString("age_category_type"),
            rs.getString("age_category_name"),
            rs.getObject("min_age", Integer.class),
            rs.getObject("max_age", Integer.class),
            rs.getString("rank_type"),
            rs.getString("rank_name"),
            rs.getObject("rank_sort_order", Integer.class),
            rs.getBigDecimal("min_distance")
    );

    public List<NormRow> findByHillAndGender(String hillType, String gender) {
        return jdbc.query(
                """
                SELECT age_category_type, age_category_name, min_age, max_age, rank_type, rank_name, rank_sort_order, min_distance
                FROM v_norms
                WHERE hill_type = ? AND gender = ?
                ORDER BY min_age, rank_sort_order
                """,
                rowMapper, hillType, gender
        );
    }
}
