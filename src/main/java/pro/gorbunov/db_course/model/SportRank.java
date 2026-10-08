package pro.gorbunov.db_course.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SportRank {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String type; // например: "CMS", "FIRST_CLASS", "MS"

    @NotBlank
    @Size(max = 100)
    private String name; // название на русском

    @Min(1) @Max(10)
    private Integer sortOrder; // порядок для сортировки (1 - низший, 10 - высший)
}
