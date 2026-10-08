package pro.gorbunov.db_course.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pro.gorbunov.db_course.model.StandardsMap;

@Repository
@RequiredArgsConstructor
public class StandardsMapRepository {

  private final JdbcTemplate jdbc;

  private final RowMapper<StandardsMap> rowMapper = (rs, rowNum) -> {
    StandardsMap s = new StandardsMap();
    s.setJumpId(rs.getLong("jump_id"));
    s.setRankId(rs.getLong("rank_id"));
    s.setJumpDate(rs.getString("jump_date"));
    return s;
  };

  private static final String BASE_SELECT = """
    SELECT
        sm.jump_id,
        sm.rank_id,
        sr.name AS rank_name,
        p.last_name || ' ' || p.first_name AS athlete_full_name,
        c.name AS competition_name,
        jr.timestamp::date::text AS jump_date
    FROM standards_map sm
    JOIN sport_rank sr   ON sr.id = sm.rank_id
    JOIN jump_result jr  ON jr.id = sm.jump_id
    JOIN athlete a       ON a.id = jr.athlete_id
    JOIN person p        ON p.id = a.id
    JOIN competition c   ON c.competition_id     = jr.competition_id
    """;

  public List<StandardsMap> findAll() {
    return jdbc.query(BASE_SELECT + "ORDER BY jr.timestamp DESC", rowMapper);
  }

  public List<StandardsMap> findByAthlete(Long athleteId) {
    return jdbc.query(
      BASE_SELECT + "WHERE jr.athlete_id = ? ORDER BY jr.timestamp DESC",
      rowMapper, athleteId
    );
  }

  public List<StandardsMap> findByRank(Long rankId) {
    return jdbc.query(
      BASE_SELECT + "WHERE sm.rank_id = ? ORDER BY jr.timestamp DESC",
      rowMapper, rankId
    );
  }

  public void create(Long jumpId, Integer rankId) {
    jdbc.update(
      "INSERT INTO standards_map (jump_id, rank_id) VALUES (?, ?)",
      jumpId, rankId
    );
  }

  public void delete(Long jumpId, Integer rankId) {
    jdbc.update(
      "DELETE FROM standards_map WHERE jump_id = ? AND rank_id = ?",
      jumpId, rankId
    );
  }
}
