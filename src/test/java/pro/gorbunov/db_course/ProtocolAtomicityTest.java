package pro.gorbunov.db_course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import pro.gorbunov.db_course.repository.JumpResultRepository;
import pro.gorbunov.db_course.repository.ProtocolRepository;

@SpringBootTest
class ProtocolAtomicityTest {

    private static final long SEEDED_COMPETITION_ID = 1L;

    @Autowired
    private ProtocolRepository protocolRepository;
    @Autowired
    private JumpResultRepository jumpResultRepository;

    @Test
    void batchWithInvalidRowSavesNothing() {
        int before = jumpResultRepository.findByCompetition(SEEDED_COMPETITION_ID).size();
        String json = "[{\"athlete_id\": 3, \"distance\": 80.0, \"attempt_number\": 3},"
                + " {\"athlete_id\": 4, \"distance\": -5, \"attempt_number\": 3}]";

        assertThrows(DataAccessException.class, () -> protocolRepository.register(SEEDED_COMPETITION_ID, json));

        assertEquals(before, jumpResultRepository.findByCompetition(SEEDED_COMPETITION_ID).size());
    }
}
