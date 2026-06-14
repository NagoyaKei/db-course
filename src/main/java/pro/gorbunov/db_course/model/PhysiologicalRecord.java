package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PhysiologicalRecord {
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
