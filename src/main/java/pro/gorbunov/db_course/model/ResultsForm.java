package pro.gorbunov.db_course.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class ResultsForm {
    public static final int ROWS = 5;

    private List<ResultEntry> entries = Stream.generate(ResultEntry::new).limit(ROWS).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
}
