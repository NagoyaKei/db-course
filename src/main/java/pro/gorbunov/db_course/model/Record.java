package pro.gorbunov.db_course.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Record {
    private Long recordId;

    @NotNull
    private Long jumpId;

    @NotBlank
    @Pattern(regexp = "^(CLUB|REGIONAL|NATIONAL|INTERNATIONAL)$")
    private String recordType;

    private LocalDate verificationDate;

    private LocalDate jumpDate;
    private BigDecimal distance;
}
