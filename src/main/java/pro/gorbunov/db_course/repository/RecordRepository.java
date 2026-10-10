package pro.gorbunov.db_course.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import pro.gorbunov.db_course.model.Record;
import pro.gorbunov.db_course.model.RecordType;

public interface RecordRepository extends ListCrudRepository<Record, Long> {

    List<Record> findByRecordType(RecordType recordType);

    Optional<Record> findByJumpId(Long jumpId);
}
