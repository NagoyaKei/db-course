package pro.gorbunov.db_course;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import pro.gorbunov.db_course.model.AthleteStatus;
import pro.gorbunov.db_course.model.CompetitionLevel;
import pro.gorbunov.db_course.model.CompetitionStatus;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.model.RecordType;

@SpringBootTest
class EnumsMatchDatabaseTest {

    private static final Pattern CODE = Pattern.compile("'([A-Z]+(?:_[A-Z]+)*)'");

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void competitionStatus() {
        assertMatchesCheck("competition", "status", CompetitionStatus.class);
    }

    @Test
    void competitionLevel() {
        assertMatchesCheck("competition", "level", CompetitionLevel.class);
    }

    @Test
    void athleteStatus() {
        assertMatchesCheck("athlete", "current_status", AthleteStatus.class);
    }

    @Test
    void recordType() {
        assertMatchesCheck("record", "type", RecordType.class);
    }

    @Test
    void gender() {
        assertMatchesCheck("person", "gender", Gender.class);
    }

    private <E extends Enum<E>> void assertMatchesCheck(String table, String column, Class<E> enumType) {
        String definition = jdbc.queryForObject(
                """
                SELECT string_agg(pg_get_constraintdef(c.oid), ' ')
                FROM pg_constraint c JOIN pg_class t ON t.oid = c.conrelid
                WHERE c.contype = 'c' AND t.relname = ? AND pg_get_constraintdef(c.oid) LIKE ?
                """,
                String.class, table, "%" + column + "%"
        );
        Set<String> expected = new TreeSet<>(Arrays.stream(enumType.getEnumConstants()).map(Enum::name).toList());
        assertEquals(expected, codesIn(definition), table + "." + column);
    }

    private Set<String> codesIn(String definition) {
        Set<String> codes = new TreeSet<>();
        Matcher matcher = CODE.matcher(definition);
        while (matcher.find()) {
            codes.add(matcher.group(1));
        }
        return codes;
    }
}
