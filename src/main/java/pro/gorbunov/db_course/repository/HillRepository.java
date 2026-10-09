package pro.gorbunov.db_course.repository;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import pro.gorbunov.db_course.model.Hill;

public interface HillRepository extends ListCrudRepository<Hill, Long> {

    @Override
    @Query("SELECT * FROM hill ORDER BY k_point, name")
    List<Hill> findAll();
}
