package pro.gorbunov.db_course.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Athlete extends Person {

    private Long coachId;

    @NotBlank
    @Size(max = 20)
    private String athleteNumber;

    @NotNull
    @Min(value = 1900, message = "Год начала не может быть раньше 1900")
    private Integer startYear;

    @NotNull
    private AthleteStatus currentStatus = AthleteStatus.ACTIVE;

    /* дополнительные поля для view */
    private String coachFullName;
    private String rankName;
}
