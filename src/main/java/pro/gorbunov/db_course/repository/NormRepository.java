package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.model.NormRow;

@Repository
@RequiredArgsConstructor
public class NormRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<NormRow> rowMapper = new DataClassRowMapper<>(NormRow.class);

    public List<NormRow> findByHillAndGender(String hillType, Gender gender) {
        return jdbc.query(
                """
                SELECT age_category_type, age_category_name, min_age, max_age, rank_type, rank_name, rank_sort_order, min_distance
                FROM v_norms
                WHERE hill_type = ? AND gender = ?
                ORDER BY min_age, rank_sort_order
                """,
                rowMapper, hillType, gender.name()
        );
    }
}
