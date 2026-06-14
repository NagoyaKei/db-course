package pro.gorbunov.db_course.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StandardsMap {
    private Long jumpId;
    private Long rankId;

    private String jumpDate;
}
