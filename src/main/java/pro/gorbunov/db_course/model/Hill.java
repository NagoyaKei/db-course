package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class Hill {
    @Id
    private Long id;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_-]{1,20}$", message = "Код: латинские заглавные буквы, цифры, - и _, до 20 символов")
    private String type;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Min(1)
    @Column("k_point")
    private Integer kpoint;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal meterValue;
}
