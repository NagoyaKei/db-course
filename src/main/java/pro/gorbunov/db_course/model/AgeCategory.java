package pro.gorbunov.db_course.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class AgeCategory {
    private Long id;

    @Size(max = 50)
    private String name; // например: "U14", "U18", "OPEN", может быть пустым

    @Min(0)
    private Integer minAge;

    @Min(0)
    private Integer maxAge;
}
