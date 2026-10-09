package pro.gorbunov.db_course.model;

import java.math.BigDecimal;

public record ProtocolRow(Long athleteId, String athleteName, Long jumpsCount, BigDecimal bestDistance, BigDecimal totalPoints, Long place) {
}
