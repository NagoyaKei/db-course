package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.SportRank;

@Repository
@RequiredArgsConstructor
public class SportRankRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<SportRank> rowMapper = (rs, rowNum) -> {
        SportRank r = new SportRank();
        r.setId(rs.getLong("id"));
        r.setType(rs.getString("type"));
        r.setName(rs.getString("name"));
        r.setSortOrder(rs.getInt("sort_order"));
        return r;
    };

    public List<SportRank> findAll() {
        return jdbc.query(
                "SELECT * FROM sport_rank r ORDER BY sort_order",
                rowMapper
        );
    }
}
