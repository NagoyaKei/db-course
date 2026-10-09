package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JumpDynamics(LocalDate jumpDate, String hillType, BigDecimal distance, BigDecimal movingAverage) {
}
