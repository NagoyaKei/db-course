package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.SportRank;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SportRankRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<SportRank> rowMapper = (rs, rowNum) -> {
        SportRank r = new SportRank();
        r.setRankId(rs.getLong("rank_id"));
        r.setRankName(rs.getString("rank_name"));
        r.setMinDistance(rs.getBigDecimal("min_distance"));
        r.setAgeCategoryId(rs.getLong("age_category_id"));
        r.setRankOrder(rs.getInt("rank_order"));
        return r;
    };

    public List<SportRank> findAll() {
        return jdbc.query(
                "SELECT * FROM sport_rank r ORDER BY rank_order",
                rowMapper
        );
    }
}
