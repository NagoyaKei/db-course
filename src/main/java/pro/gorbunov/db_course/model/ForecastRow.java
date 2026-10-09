package pro.gorbunov.db_course.model;

import java.math.BigDecimal;

public record ForecastRow(Long athleteId, String athleteName, String rankName, BigDecimal forecastDistance, BigDecimal trendPerYear,
                          Integer jumpsUsed, BigDecimal rSquared) {
}
