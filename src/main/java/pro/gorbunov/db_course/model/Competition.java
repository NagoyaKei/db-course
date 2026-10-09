package pro.gorbunov.db_course.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class Competition {

    @Id
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    @Column("date")
    private LocalDate competitionDate;

    @Size(max = 200)
    private String location;

    private Long hillId;

    @ReadOnlyProperty
    private String hillName;

    @NotNull
    private CompetitionLevel level = CompetitionLevel.LOCAL;

    @NotNull
    private CompetitionStatus status = CompetitionStatus.PLANNED;

    @Min(1)
    private Integer maxParticipants;

    @ReadOnlyProperty
    private Integer participantsCount;

    public String getTitle() {
        return name + " (" + competitionDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ")";
    }
}
