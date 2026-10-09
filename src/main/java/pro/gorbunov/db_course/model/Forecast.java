package pro.gorbunov.db_course.model;

import java.math.BigDecimal;

public record Forecast(BigDecimal forecastDistance, BigDecimal trendPerYear, Integer jumpsUsed, BigDecimal rSquared) {
}
