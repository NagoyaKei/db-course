package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgeRecord(String ageCategoryType, String ageCategoryName, String gender, String hillType, String hillName, Long athleteId,
                        String athleteName, BigDecimal distance, LocalDate jumpDate, String competitionName, String recordType) {
}
