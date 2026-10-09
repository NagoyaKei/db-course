package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class PhysiologicalRecord {
    @Id
    @Column("id")
    private Long recordId;

    @NotNull
    private Long athleteId; // FK -> ATHLETE

    @NotNull
    @PastOrPresent
    private LocalDate measureDate;

    @NotNull
    @DecimalMin("1.00") @DecimalMax("300.00")
    private BigDecimal height; // рост в сантиметрах

    @NotNull
    @DecimalMin("1.0") @DecimalMax("300.0")
    private BigDecimal weight;

    @DecimalMin("0.0")
    private BigDecimal vo2Max;

    @DecimalMin("0.0")
    private BigDecimal restingHr;

    @Size(max = 500)
    private String notes;
}
