package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SportRank {
    private Long rankId;

    @NotBlank
    @Size(max = 100)
    private String rankName; // например: "КМС", "1 разряд", "МС"

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal minDistance; // минимальная дистанция для получения разряда

    private Long ageCategoryId; // FK -> AGE_CATEGORY

    @Min(1) @Max(10)
    private Integer rankOrder; // порядок для сортировки (1 - низший, 10 - высший)
}
