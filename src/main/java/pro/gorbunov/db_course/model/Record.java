package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Record {
    private Long recordId;

    @NotNull
    private Long jumpId;

    @NotBlank
    @Pattern(regexp = "^(club|regional|national|international)$")
    private String recordType;

    private LocalDate verificationDate;

    private LocalDate jumpDate;
    private BigDecimal distance;
}
