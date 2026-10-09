package pro.gorbunov.db_course.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pro.gorbunov.db_course.model.JumpResult;

public interface JumpResultRepository extends Repository<JumpResult, Long> {

    String BASE_SELECT = """
            SELECT jr.id, jr.athlete_id, jr.competition_id, jr.distance, jr.style_points, jr.wind_factor, jr.gate_mark,
                   jr.timestamp, jr.attempt_number,
                   p.last_name || ' ' || p.first_name AS athlete_full_name,
                   c.name AS competition_name,
                   jc.hill_type, jc.age_category_name, jc.points
            FROM jump_result jr
            JOIN person p ON p.id = jr.athlete_id
            JOIN competition c ON c.id = jr.competition_id
            JOIN v_jump_context jc ON jc.jump_id = jr.id
            """;

    @Query(BASE_SELECT + "ORDER BY jr.timestamp DESC")
    List<JumpResult> findAll();

    @Query(BASE_SELECT + "WHERE jr.id = :id")
    Optional<JumpResult> findById(@Param("id") Long id);

    @Query(BASE_SELECT + "ORDER BY jr.timestamp DESC LIMIT 6")
    List<JumpResult> findLatest();

    @Query(BASE_SELECT + "WHERE jr.athlete_id = :athleteId ORDER BY jr.timestamp DESC")
    List<JumpResult> findByAthlete(@Param("athleteId") Long athleteId);

    @Query(BASE_SELECT + "WHERE jr.competition_id = :competitionId ORDER BY jr.distance DESC")
    List<JumpResult> findByCompetition(@Param("competitionId") Long competitionId);

    JumpResult save(JumpResult jump);

    void deleteById(Long id);

    long count();
}
