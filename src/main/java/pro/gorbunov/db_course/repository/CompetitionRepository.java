package pro.gorbunov.db_course.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pro.gorbunov.db_course.model.Competition;

public interface CompetitionRepository extends Repository<Competition, Long> {

    String BASE_SELECT = """
            SELECT c.id, c.name, c.location, c.date, c.hill_id, h.name AS hill_name, c.level, c.max_participants, c.status,
                   COUNT(DISTINCT jr.athlete_id) AS participants_count
            FROM competition c
            LEFT JOIN hill h ON h.id = c.hill_id
            LEFT JOIN jump_result jr ON jr.competition_id = c.id
            GROUP BY c.id, c.date, h.id
            """;

    @Query(BASE_SELECT + "ORDER BY c.date DESC")
    List<Competition> findAll();

    @Query(BASE_SELECT + "HAVING c.id = :id")
    Optional<Competition> findById(@Param("id") Long id);

    Competition save(Competition competition);

    void deleteById(Long id);

    long count();
}
