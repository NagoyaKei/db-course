package pro.gorbunov.db_course;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pro.gorbunov.db_course.model.Athlete;
import pro.gorbunov.db_course.model.AthleteStatus;
import pro.gorbunov.db_course.model.Coach;
import pro.gorbunov.db_course.model.Competition;
import pro.gorbunov.db_course.model.CompetitionLevel;
import pro.gorbunov.db_course.model.CompetitionStatus;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.model.JumpResult;
import pro.gorbunov.db_course.model.Person;
import pro.gorbunov.db_course.model.PhysiologicalRecord;
import pro.gorbunov.db_course.model.Record;
import pro.gorbunov.db_course.model.RecordType;
import pro.gorbunov.db_course.model.SportRank;
import pro.gorbunov.db_course.model.StoredImage;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;
import pro.gorbunov.db_course.repository.PersonImageRepository;
import pro.gorbunov.db_course.repository.PhysiologicalRecordRepository;
import pro.gorbunov.db_course.repository.RecordRepository;
import pro.gorbunov.db_course.repository.SportRankRepository;

@SpringBootTest
@Transactional
class RepositoriesTest {

    private static final long SEEDED_ATHLETE_ID = 3L;
    private static final long SEEDED_COMPETITION_ID = 1L;

    @Autowired
    private HillRepository hillRepository;
    @Autowired
    private CompetitionRepository competitionRepository;
    @Autowired
    private JumpResultRepository jumpResultRepository;
    @Autowired
    private PhysiologicalRecordRepository physiologicalRecordRepository;
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private SportRankRepository sportRankRepository;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private PersonImageRepository personImageRepository;

    @Test
    void hillIsSavedUpdatedAndDeleted() {
        Hill hill = new Hill();
        hill.setType("T" + System.nanoTime() % 1_000_000);
        hill.setName("Test hill");
        hill.setKpoint(70);
        hill.setMeterValue(new BigDecimal("2.10"));

        Long id = hillRepository.save(hill).getId();
        assertNotNull(id);
        assertEquals(70, hillRepository.findById(id).orElseThrow().getKpoint());

        hill.setName("Renamed");
        hillRepository.save(hill);
        assertEquals("Renamed", hillRepository.findById(id).orElseThrow().getName());

        hillRepository.deleteById(id);
        assertTrue(hillRepository.findById(id).isEmpty());
    }

    @Test
    void hillsAreOrderedByKPoint() {
        List<Integer> kpoints = hillRepository.findAll().stream().map(Hill::getKpoint).toList();

        assertEquals(kpoints.stream().sorted().toList(), kpoints);
    }

    @Test
    void competitionHasDefaultsAndJoinedFields() {
        Competition competition = new Competition();
        competition.setName("Test cup");
        competition.setCompetitionDate(LocalDate.of(2026, 11, 1));
        competition.setHillId(hillRepository.findAll().get(0).getId());

        Long id = competitionRepository.save(competition).getId();
        Competition found = competitionRepository.findById(id).orElseThrow();

        assertEquals(CompetitionLevel.LOCAL, found.getLevel());
        assertEquals(CompetitionStatus.PLANNED, found.getStatus());
        assertNotNull(found.getHillName());
        assertEquals(0, found.getParticipantsCount());
        assertEquals("Test cup (01.11.2026)", found.getTitle());

        found.setStatus(CompetitionStatus.CANCELLED);
        competitionRepository.save(found);
        assertEquals(CompetitionStatus.CANCELLED, competitionRepository.findById(id).orElseThrow().getStatus());

        competitionRepository.deleteById(id);
        assertTrue(competitionRepository.findById(id).isEmpty());
    }

    @Test
    void jumpIsSavedWithJoinedFieldsAndAssignsRank() {
        Long id = jumpResultRepository.save(newJump(new BigDecimal("95.50"))).getJumpId();

        JumpResult found = jumpResultRepository.findById(id).orElseThrow();
        assertEquals(new BigDecimal("95.50"), found.getDistance());
        assertNotNull(found.getAthleteFullName());
        assertNotNull(found.getCompetitionName());
        assertNotNull(found.getHillType());
        assertNotNull(found.getPoints());
        assertTrue(jumpResultRepository.findByAthlete(SEEDED_ATHLETE_ID).stream().anyMatch(jump -> jump.getJumpId().equals(id)));
        assertTrue(jumpResultRepository.findByCompetition(SEEDED_COMPETITION_ID).stream().anyMatch(jump -> jump.getJumpId().equals(id)));
        assertEquals(6, jumpResultRepository.findLatest().size());

        jumpResultRepository.deleteById(id);
        assertTrue(jumpResultRepository.findById(id).isEmpty());
    }

    @Test
    void physiologicalRecordIsFoundByAthlete() {
        PhysiologicalRecord record = new PhysiologicalRecord();
        record.setAthleteId(SEEDED_ATHLETE_ID);
        record.setMeasureDate(LocalDate.of(2026, 9, 25));
        record.setHeight(new BigDecimal("176.5"));
        record.setWeight(new BigDecimal("69.0"));

        Long id = physiologicalRecordRepository.save(record).getRecordId();

        assertEquals(id, physiologicalRecordRepository.findByAthleteIdOrderByMeasureDateDesc(SEEDED_ATHLETE_ID).get(0).getRecordId());
        assertEquals(id, physiologicalRecordRepository.findFirstByAthleteIdOrderByMeasureDateDesc(SEEDED_ATHLETE_ID).orElseThrow().getRecordId());
    }

    @Test
    void recordIsSavedWithTypeAndDefaultDate() {
        Long jumpId = jumpResultRepository.save(newJump(new BigDecimal("90.00"))).getJumpId();
        Record record = new Record();
        record.setJumpId(jumpId);
        record.setRecordType(RecordType.REGIONAL);

        Long id = recordRepository.save(record).getRecordId();

        Record found = recordRepository.findById(id).orElseThrow();
        assertEquals(RecordType.REGIONAL, found.getRecordType());
        assertNotNull(found.getVerificationDate());
        assertTrue(recordRepository.findByRecordType(RecordType.REGIONAL).stream().anyMatch(candidate -> candidate.getRecordId().equals(id)));
    }

    @Test
    void sportRanksAreOrderedBySortOrder() {
        List<Integer> orders = sportRankRepository.findAllByOrderBySortOrder().stream().map(SportRank::getSortOrder).toList();

        assertEquals(5, orders.size());
        assertEquals(orders.stream().sorted().toList(), orders);
    }

    @Test
    void athleteWithoutCoachIsSavedAndUpdated() {
        Athlete athlete = new Athlete();
        fillPerson(athlete, "Athlete");
        athlete.setAthleteNumber("ATH-TEST-" + System.nanoTime() % 1_000_000);
        athlete.setStartYear(2020);
        athlete.setCurrentStatus(AthleteStatus.SUSPENDED);

        Long id = athleteRepository.create(athlete);
        Athlete found = athleteRepository.findById(id).orElseThrow();

        assertEquals(AthleteStatus.SUSPENDED, found.getCurrentStatus());
        assertEquals(Gender.F, found.getGender());
        assertNull(found.getCoachId());
        assertEquals(athlete.getBirthDate(), found.getBirthDate());

        found.setCurrentStatus(AthleteStatus.ACTIVE);
        found.setCoachId(1L);
        found.setGender(Gender.M);
        athleteRepository.update(found);
        Athlete updated = athleteRepository.findById(id).orElseThrow();
        assertEquals(AthleteStatus.ACTIVE, updated.getCurrentStatus());
        assertEquals(1L, updated.getCoachId());
        assertEquals(Gender.M, updated.getGender());
        assertTrue(athleteRepository.findByStatus(AthleteStatus.ACTIVE).stream().anyMatch(candidate -> candidate.getPersonId().equals(id)));

        updated.setCurrentStatus(AthleteStatus.INACTIVE);
        athleteRepository.update(updated);

        assertEquals(AthleteStatus.INACTIVE, athleteRepository.findById(id).orElseThrow().getCurrentStatus());
    }

    @Test
    void coachIsSavedWithDefaultExperience() {
        Coach coach = new Coach();
        fillPerson(coach, "Coach");
        coach.setLicenseNumber("LIC-TEST-" + System.nanoTime() % 1_000_000);

        Long id = coachRepository.create(coach);

        Coach found = coachRepository.findById(id).orElseThrow();
        assertEquals(0, found.getExperienceYears());
        assertEquals(Gender.F, found.getGender());

        found.setExperienceYears(12);
        coachRepository.update(found);
        assertEquals(12, coachRepository.findById(id).orElseThrow().getExperienceYears());
    }

    @Test
    void personPhotoIsSavedReplacedAndDeleted() {
        long personId = SEEDED_ATHLETE_ID;
        StoredImage first = new StoredImage("image/png", new byte[]{1, 2, 3});
        StoredImage second = new StoredImage("image/jpeg", new byte[]{4, 5});

        personImageRepository.save(personId, first);
        personImageRepository.save(personId, second);

        StoredImage found = personImageRepository.find(personId).orElseThrow();
        assertEquals("image/jpeg", found.contentType());
        assertEquals(2, found.data().length);
        assertTrue(personImageRepository.exists(personId));

        personImageRepository.delete(personId);
        assertFalse(personImageRepository.exists(personId));
    }

    private JumpResult newJump(BigDecimal distance) {
        JumpResult jump = new JumpResult();
        jump.setAthleteId(SEEDED_ATHLETE_ID);
        jump.setCompetitionId(SEEDED_COMPETITION_ID);
        jump.setDistance(distance);
        jump.setStylePoints(new BigDecimal("50.0"));
        jump.setAttemptNumber(3);
        return jump;
    }

    private void fillPerson(Person person, String firstName) {
        person.setLastName("Test");
        person.setFirstName(firstName);
        person.setBirthDate(LocalDate.of(1995, 4, 12));
        person.setGender(Gender.F);
    }
}
