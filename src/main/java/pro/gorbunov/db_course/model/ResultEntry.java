package pro.gorbunov.db_course.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ResultEntry {
    private Long athleteId;
    private BigDecimal distance;
    private Integer attemptNumber = 1;
    private BigDecimal stylePoints;
    private BigDecimal windFactor;

    public boolean isBlank() {
        return athleteId == null && distance == null;
    }

    public boolean isComplete() {
        return athleteId != null && distance != null;
    }
}
