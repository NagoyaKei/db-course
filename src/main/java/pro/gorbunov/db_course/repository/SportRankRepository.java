package pro.gorbunov.db_course.repository;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import pro.gorbunov.db_course.model.SportRank;

public interface SportRankRepository extends ListCrudRepository<SportRank, Long> {

    List<SportRank> findAllByOrderBySortOrder();
}
