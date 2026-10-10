package pro.gorbunov.db_course.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Coach extends Person {

    @NotBlank
    @Size(max = 50)
    private String licenseNumber;

    @NotNull
    @Min(0) @Max(80)
    private Integer experienceYears = 0;

    private Long athleteId;
    private String rankName;
}