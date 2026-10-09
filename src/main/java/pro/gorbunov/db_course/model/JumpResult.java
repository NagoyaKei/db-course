package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class JumpResult {
    @Id
    @Column("id")
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

    @Column("timestamp")
    private LocalDateTime jumpTimestamp = LocalDateTime.now();

    @Min(1)
    private Integer attemptNumber;

    private String gateMark;

    @ReadOnlyProperty
    private String athleteFullName;
    @ReadOnlyProperty
    private String competitionName;
    @ReadOnlyProperty
    private String hillType;
    @ReadOnlyProperty
    private String ageCategoryName;
    @ReadOnlyProperty
    private BigDecimal points;
}
