package pro.gorbunov.db_course;

import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;

@SpringBootTest
class SeedDataPassesValidationTest {

    @Autowired
    private Validator validator;
    @Autowired
    private AthleteRepository athleteRepository;
    @Autowired
    private CoachRepository coachRepository;
    @Autowired
    private CompetitionRepository competitionRepository;
    @Autowired
    private HillRepository hillRepository;
    @Autowired
    private JumpResultRepository jumpResultRepository;

    @Test
    void everyStoredEntityIsValid() {
        List<String> violations = Stream.of(
                        athleteRepository.findAll(),
                        coachRepository.findAll(),
                        competitionRepository.findAll(),
                        hillRepository.findAll(),
                        jumpResultRepository.findAll())
                .flatMap(List::stream)
                .flatMap(entity -> validator.validate(entity).stream().map(violation -> describe(entity, (ConstraintViolation<Object>) violation)))
                .toList();

        assertTrue(violations.isEmpty(), () -> String.join("\n", violations));
    }

    private String describe(Object entity, ConstraintViolation<Object> violation) {
        return entity.getClass().getSimpleName() + "." + violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
