package pro.gorbunov.db_course.model;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class Record {
    @Id
    @Column("id")
    private Long recordId;

    @NotNull
    private Long jumpId;

    @NotNull
    @Column("type")
    private RecordType recordType;

    private LocalDate verificationDate = LocalDate.now();
}
