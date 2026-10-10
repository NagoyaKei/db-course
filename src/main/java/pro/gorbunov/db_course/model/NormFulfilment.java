package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NormFulfilment(LocalDate jumpDate, String rankName, String hillType, BigDecimal distance, LocalDate measureDate,
                             BigDecimal height, BigDecimal weight) {
}
