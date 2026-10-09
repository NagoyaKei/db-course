package pro.gorbunov.db_course.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class SportRank {
    @Id
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
