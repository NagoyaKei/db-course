package pro.gorbunov.db_course.controller;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;

@Controller
@AllArgsConstructor
public class HomeController {

    private static final String JUMP_COUNT_SQL = "SELECT COUNT(*) FROM jump_result";

    private final JdbcTemplate jdbcTemplate;
    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final CompetitionRepository competitionRepository;

    @GetMapping("/")
    public String home(Model model) {
        try {
            String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
            model.addAttribute("status", "OK");
            model.addAttribute("version", version);
        } catch (Exception e) {
            model.addAttribute("status", "ERROR");
        }
        model.addAttribute("athleteCount",     athleteRepository.count());
        model.addAttribute("coachCount",       coachRepository.count());
        model.addAttribute("competitionCount", competitionRepository.count());
        model.addAttribute("jumpCount",        jdbcTemplate.queryForObject(JUMP_COUNT_SQL, Integer.class));
        return "index";
    }
}