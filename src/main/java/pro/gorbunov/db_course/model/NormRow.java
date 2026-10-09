package pro.gorbunov.db_course.model;

import java.math.BigDecimal;

public record NormRow(String ageCategoryType, String ageCategoryName, Integer minAge, Integer maxAge, String rankType, String rankName,
                      Integer rankSortOrder, BigDecimal minDistance) {
}
