package pro.gorbunov.db_course.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.PhysiologicalRecord;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PhysiologicalRecordRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<PhysiologicalRecord> rowMapper = (rs, rowNum) -> {
        PhysiologicalRecord r = new PhysiologicalRecord();
        r.setRecordId(rs.getLong("record_id"));
        r.setAthleteId(rs.getLong("athlete_id"));
        r.setMeasureDate(rs.getDate("measure_date").toLocalDate());
        r.setHeight(rs.getBigDecimal("height"));
        r.setWeight(rs.getBigDecimal("weight"));
        r.setVo2Max(rs.getBigDecimal("vo2_max"));
        r.setRestingHr(rs.getBigDecimal("resting_hr"));
        r.setNotes(rs.getString("notes"));
        return r;
    };

    public List<PhysiologicalRecord> findByAthlete(Long athleteId) {
        return jdbc.query(
                """
                SELECT * FROM physiological_record
                WHERE athlete_id = ?
                ORDER BY measure_date DESC
                """,
                rowMapper, athleteId
        );
    }

    public Optional<PhysiologicalRecord> findLatestByAthlete(Long athleteId) {
        return jdbc.query(
                """
                SELECT * FROM physiological_record
                WHERE athlete_id = ?
                ORDER BY measure_date DESC
                LIMIT 1
                """,
                rowMapper, athleteId
        ).stream().findFirst();
    }

    public Long create(PhysiologicalRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    """
                    INSERT INTO physiological_record (athlete_id, measure_date, height, weight, notes)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, record.getAthleteId());
            ps.setDate(2, java.sql.Date.valueOf(record.getMeasureDate()));
            ps.setBigDecimal(3, record.getHeight());
            ps.setBigDecimal(4, record.getWeight());
            ps.setString(5, record.getNotes());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM physiological_record WHERE record_id = ?", id);
    }
}