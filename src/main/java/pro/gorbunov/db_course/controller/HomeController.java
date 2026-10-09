package pro.gorbunov.db_course.controller;

import java.time.LocalDate;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pro.gorbunov.db_course.model.Competition;
import pro.gorbunov.db_course.model.CompetitionStatus;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final CompetitionRepository competitionRepository;
    private final JumpResultRepository jumpResultRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("athleteCount", athleteRepository.count());
        model.addAttribute("coachCount", coachRepository.count());
        model.addAttribute("competitionCount", competitionRepository.count());
        model.addAttribute("jumpCount", jumpResultRepository.count());
        model.addAttribute("latestJumps", jumpResultRepository.findLatest());
        model.addAttribute("nextCompetition", findNextCompetition());
        return "index";
    }

    private Competition findNextCompetition() {
        LocalDate today = LocalDate.now();
        return competitionRepository.findAll().stream()
                .filter(competition -> competition.getStatus() == CompetitionStatus.PLANNED)
                .filter(competition -> !competition.getCompetitionDate().isBefore(today))
                .min(Comparator.comparing(Competition::getCompetitionDate))
                .orElse(null);
    }
}
