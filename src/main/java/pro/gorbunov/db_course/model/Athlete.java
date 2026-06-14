package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Athlete extends Person {

    @NotNull
    private Long coachId;     // FK -> COACH

    @NotBlank
    @Size(max = 20)
    private String athleteNumber;

    @NotNull
    @Min(value = 2000, message = "Год начала не может быть раньше 2000")
    private Integer startYear;

    @NotBlank
    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$")
    private String currentStatus;
}
