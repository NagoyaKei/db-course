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
    @Min(value = 1900, message = "Год начала не может быть раньше 1900")
    private Integer startYear;

    @NotBlank
    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)$")
    private String currentStatus;

    /* дополнительные поля для view */
    private String coachFullName;
    private String rankName;
}
