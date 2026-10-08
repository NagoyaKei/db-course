package pro.gorbunov.db_course.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Competition {

    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    private LocalDate competitionDate;

    @Size(max = 200)
    private String location;

    private Long hillId;

    private String hillName;

    @NotBlank
    @Pattern(regexp = "^(LOCAL|REGIONAL|NATIONAL|INTERNATIONAL)$")
    private String level;

    @NotBlank
    @Pattern(regexp = "^(PLANNED|ACTIVE|FINISHED|CANCELLED)$")
    private String status;

    @Min(1)
    private Integer maxParticipants;

    // Вычисляемое поле - количество участников (из JOIN)
    private Integer participantsCount;
}
