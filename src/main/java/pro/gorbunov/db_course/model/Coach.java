package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Coach extends Person {

    @NotBlank
    @Size(max = 50)
    private String licenseNumber;

    @NotBlank
    @Size(max = 100)
    private String specialization;

    @Min(0) @Max(50)
    private Integer experienceYears;
}