package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Hill {
    private Long id;
    private String type;
    private String name;
    private Integer kpoint;
    private BigDecimal meterValue;
}
