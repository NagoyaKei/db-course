package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Hill;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HillRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Hill> rowMapper = (rs, rowNum) -> {
        Hill h = new Hill();
        h.setId(rs.getLong("id"));
        h.setType(rs.getString("type"));
        h.setName(rs.getString("name"));
        h.setKpoint(rs.getInt("k_point"));
        h.setMeterValue(rs.getBigDecimal("meter_value"));
        return h;
    };

    private static final String BASE_SELECT = """
            SELECT h.id, h.type, h.name, h.k_point, h.meter_value
            FROM hill h
            """;

    public List<Hill> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY h.k_point, h.name", rowMapper);
    }

    public Optional<Hill> findById(Long id) {
        return jdbc.query(BASE_SELECT + "WHERE h.id = ?", rowMapper, id).stream().findFirst();
    }

    public Long create(Hill hill) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO hill (type, name, k_point, meter_value) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, hill.getType());
            ps.setString(2, hill.getName());
            ps.setInt(3, hill.getKpoint());
            ps.setBigDecimal(4, hill.getMeterValue());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void update(Hill hill) {
        jdbc.update(
                "UPDATE hill SET type = ?, name = ?, k_point = ?, meter_value = ? WHERE id = ?",
                hill.getType(), hill.getName(), hill.getKpoint(), hill.getMeterValue(), hill.getId()
        );
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM hill WHERE id = ?", id);
    }
}
