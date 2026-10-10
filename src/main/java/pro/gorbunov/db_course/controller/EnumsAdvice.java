package pro.gorbunov.db_course.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pro.gorbunov.db_course.model.AthleteStatus;
import pro.gorbunov.db_course.model.CompetitionLevel;
import pro.gorbunov.db_course.model.CompetitionStatus;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.model.RecordType;

@ControllerAdvice
public class EnumsAdvice {

    @ModelAttribute("enums")
    public Map<String, Enum<?>[]> enums() {
        return Map.of(
                "gender", Gender.values(),
                "athleteStatus", AthleteStatus.values(),
                "competitionLevel", CompetitionLevel.values(),
                "competitionStatus", CompetitionStatus.values(),
                "recordType", RecordType.values()
        );
    }
}
