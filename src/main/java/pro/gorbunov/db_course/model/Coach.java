package pro.gorbunov.db_course.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Coach extends Person {

    @NotBlank
    @Size(max = 50)
    private String licenseNumber;

    @Min(0) @Max(50)
    private Integer experienceYears;
}