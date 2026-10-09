package pro.gorbunov.db_course.model;

import java.math.BigDecimal;

public record CorrelationRow(String hillType, Long jumpsCount, BigDecimal heightCorrelation, BigDecimal weightCorrelation) {
}
