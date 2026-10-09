package pro.gorbunov.db_course.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import pro.gorbunov.db_course.model.PhysiologicalRecord;

public interface PhysiologicalRecordRepository extends ListCrudRepository<PhysiologicalRecord, Long> {

    List<PhysiologicalRecord> findByAthleteIdOrderByMeasureDateDesc(Long athleteId);

    Optional<PhysiologicalRecord> findFirstByAthleteIdOrderByMeasureDateDesc(Long athleteId);
}
