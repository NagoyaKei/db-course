package pro.gorbunov.db_course.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.Record;

@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Record> rowMapper = (rs, rowNum) -> {
        Record r = new Record();
        r.setRecordId(rs.getLong("record_id"));
        r.setJumpId(rs.getLong("jump_id"));
        r.setRecordType(rs.getString("record_type"));
        r.setVerificationDate(rs.getDate("verification_date").toLocalDate());
        r.setDistance(rs.getBigDecimal("distance"));
        java.sql.Date jd = rs.getDate("jump_date");
        r.setJumpDate(jd != null ? jd.toLocalDate() : null);
        return r;
    };

    private static final String BASE_SELECT = """
            SELECT
                r.id AS record_id,
                r.jump_id,
                r.type AS record_type,
                r.verification_date,
                jr.distance,
                jr.timestamp::date AS jump_date
            FROM record r
            JOIN jump_result jr  ON jr.id = r.jump_id
            JOIN athlete a       ON a.id = jr.athlete_id
            JOIN person p        ON p.id = a.id
            JOIN competition c   ON c.competition_id   = jr.competition_id
            """;

    public List<Record> findAll() {
        return jdbc.query(BASE_SELECT + "ORDER BY jr.distance DESC", rowMapper);
    }

    public List<Record> findByType(String recordType) {
        return jdbc.query(
                BASE_SELECT + "WHERE r.type = ? ORDER BY jr.distance DESC",
                rowMapper, recordType
        );
    }

    public Optional<Record> findById(Long id) {
        return jdbc.query(
                BASE_SELECT + "WHERE r.id = ?",
                rowMapper, id
        ).stream().findFirst();
    }

    public Integer create(Record record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO record (jump_id, type, verification_date)
                    VALUES (?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, record.getJumpId());
            ps.setString(2, record.getRecordType());
            ps.setDate(3, record.getVerificationDate() != null
                    ? java.sql.Date.valueOf(record.getVerificationDate())
                    : java.sql.Date.valueOf(java.time.LocalDate.now()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM record WHERE id = ?", id);
    }
}
