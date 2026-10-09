package pro.gorbunov.db_course.model;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JumpResult {
    private Long jumpId;

    @NotNull
    private Long athleteId;   // FK -> ATHLETE

    @NotNull
    private Long competitionId; // FK -> COMPETITION

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax(value = "300.0")
    private BigDecimal distance; // дистанция прыжка в метрах

    @DecimalMin("0.0") @DecimalMax("60.0")
    private BigDecimal stylePoints; // судейские баллы за стиль

    @DecimalMin("-5.0") @DecimalMax("5.0")
    private BigDecimal windFactor; // поправка на ветер

    private LocalDateTime jumpTimestamp;

    @Min(1)
    private Integer attemptNumber;

    private String gateMark;

    /* additional fields for view */
    private String athleteFullName;
    private String competitionName;
    private String hillType;
    private String ageCategoryName;
    private BigDecimal points;
}
