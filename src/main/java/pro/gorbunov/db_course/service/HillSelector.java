package pro.gorbunov.db_course.service;

import java.util.List;
import org.springframework.stereotype.Component;
import pro.gorbunov.db_course.model.Hill;

@Component
public class HillSelector {

    public Hill select(List<Hill> hills, String type) {
        return hills.stream()
                .filter(candidate -> candidate.getType().equals(type))
                .findFirst()
                .orElse(hills.isEmpty() ? null : hills.get(0));
    }
}
