package pro.gorbunov.db_course;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import pro.gorbunov.db_course.model.Athlete;
import pro.gorbunov.db_course.model.AthleteStatus;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.repository.AthleteRepository;

@SpringBootTest
@Transactional
class DatabaseRulesTest {

    private static final long SEEDED_ATHLETE_ID = 3L;
    private static final long VETERAN_ID = 7L;
    private static final long FINISHED_COMPETITION_ID = 1L;
    private static final long PLANNED_COMPETITION_ID = 4L;
    private static final long CANCELLED_COMPETITION_ID = 11L;
    private static final String FINISHED_COMPETITION_DAY = "2024-02-10";
    private static final String MASTER_RANK = "Мастер спорта";

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private AthleteRepository athleteRepository;

    @Test
    void secondJumpWithSameAttemptIsRejected() {
        insertJump(SEEDED_ATHLETE_ID, FINISHED_COMPETITION_ID, FINISHED_COMPETITION_DAY + " 18:00:00", 9);

        assertThrows(DataIntegrityViolationException.class,
                () -> insertJump(SEEDED_ATHLETE_ID, FINISHED_COMPETITION_ID, FINISHED_COMPETITION_DAY + " 19:00:00", 9));
    }

    @Test
    void jumpInPlannedCompetitionIsRejected() {
        assertThrows(DataIntegrityViolationException.class, () -> insertJump(SEEDED_ATHLETE_ID, PLANNED_COMPETITION_ID, null, 1));
    }

    @Test
    void jumpInCancelledCompetitionIsRejected() {
        assertThrows(DataIntegrityViolationException.class, () -> insertJump(SEEDED_ATHLETE_ID, CANCELLED_COMPETITION_ID, null, 1));
    }

    @Test
    void jumpOnAnotherDayIsRejected() {
        assertThrows(DataIntegrityViolationException.class,
                () -> insertJump(SEEDED_ATHLETE_ID, FINISHED_COMPETITION_ID, "2024-02-11 10:00:00", 9));
    }

    @Test
    void jumpWithoutTimestampGetsCompetitionDate() {
        insertJump(SEEDED_ATHLETE_ID, FINISHED_COMPETITION_ID, null, 9);

        LocalDate jumpDate = jdbc.queryForObject(
                "SELECT timestamp::DATE FROM jump_result WHERE athlete_id = ? AND competition_id = ? AND attempt_number = 9",
                LocalDate.class, SEEDED_ATHLETE_ID, FINISHED_COMPETITION_ID);

        assertEquals(LocalDate.parse(FINISHED_COMPETITION_DAY), jumpDate);
    }

    @Test
    void jumpAboveNormGivesRank() {
        Long athleteId = newAthleteWithoutJumps();

        insertJumpWithDistance(athleteId, "95.00");

        assertEquals(MASTER_RANK, jdbc.queryForObject("SELECT rank_name FROM v_athlete_current_rank WHERE athlete_id = ?", String.class, athleteId));
    }

    @Test
    void normFulfilmentHasMeasureTakenNoLaterThanTheJump() {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM v_norm_fulfilment WHERE athlete_id = ?", VETERAN_ID);

        assertFalse(rows.isEmpty());
        assertTrue(rows.stream().anyMatch(row -> MASTER_RANK.equals(row.get("rank_name"))));
        assertTrue(rows.stream().allMatch(row -> row.get("measure_date") == null
                || !((java.sql.Date) row.get("measure_date")).after((java.sql.Date) row.get("jump_date"))));
    }

    @Test
    void measureBeforeBirthIsRejected() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO physiological_record (athlete_id, measure_date, height, weight) VALUES (?, DATE '2000-01-01', 170, 60)",
                SEEDED_ATHLETE_ID));
    }

    @Test
    void startYearBeforeBirthYearIsRejected() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE athlete SET start_year = 1990 WHERE id = ?", SEEDED_ATHLETE_ID));
    }

    @Test
    void forecastIsCalculatedOnlyForAthleteWithJumps() {
        Map<String, Object> withJumps = forecast(SEEDED_ATHLETE_ID);
        Map<String, Object> withoutJumps = forecast(newAthleteWithoutJumps());

        assertNotNull(withJumps.get("forecast_distance"));
        assertNotNull(withJumps.get("trend_per_year"));
        assertNull(withoutJumps.get("forecast_distance"));
    }

    private Map<String, Object> forecast(Long athleteId) {
        return jdbc.queryForMap("CALL p_calculate_forecast(?, 90, NULL, NULL, NULL, NULL)", athleteId);
    }

    private void insertJump(long athleteId, long competitionId, String timestamp, int attempt) {
        jdbc.update("INSERT INTO jump_result (athlete_id, competition_id, timestamp, distance, attempt_number) VALUES (?, ?, ?::TIMESTAMP, 70, ?)",
                athleteId, competitionId, timestamp, attempt);
    }

    private void insertJumpWithDistance(long athleteId, String distance) {
        jdbc.update("INSERT INTO jump_result (athlete_id, competition_id, distance, attempt_number) VALUES (?, ?, ?::NUMERIC, 1)",
                athleteId, FINISHED_COMPETITION_ID, distance);
    }

    private Long newAthleteWithoutJumps() {
        Athlete athlete = new Athlete();
        athlete.setLastName("Test");
        athlete.setFirstName("Athlete");
        athlete.setBirthDate(LocalDate.of(1995, 4, 12));
        athlete.setGender(Gender.F);
        athlete.setAthleteNumber("ATH-RULES-" + System.nanoTime() % 1_000_000);
        athlete.setStartYear(2020);
        athlete.setCurrentStatus(AthleteStatus.ACTIVE);
        return athleteRepository.create(athlete);
    }
}
