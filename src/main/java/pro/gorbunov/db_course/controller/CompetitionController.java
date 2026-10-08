package pro.gorbunov.db_course.controller;

import javax.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.model.Competition;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;

@Controller
@RequestMapping("/competitions")
public class CompetitionController {

    private final CompetitionRepository competitionRepository;
    private final JumpResultRepository jumpResultRepository;
    private final HillRepository hillRepository;

    public CompetitionController(CompetitionRepository competitionRepository,
                                 JumpResultRepository jumpResultRepository,
                                 HillRepository hillRepository) {
        this.competitionRepository = competitionRepository;
        this.jumpResultRepository = jumpResultRepository;
        this.hillRepository = hillRepository;
    }

    @ModelAttribute("hills")
    public List<Hill> hills() {
        return hillRepository.findAll();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("competitions", competitionRepository.findAll());
        return "competitions/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("competition", competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Соревнование не найдено: " + id)));
        model.addAttribute("results", jumpResultRepository.findByCompetition(id));
        return "competitions/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Competition competition = new Competition();
        competition.setStatus("PLANNED");
        competition.setLevel("LOCAL");
        model.addAttribute("competition", competition);
        return "competitions/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Competition competition,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "competitions/form";
        Long id = competitionRepository.create(competition);
        redirect.addFlashAttribute("success", "Соревнование добавлено");
        return "redirect:/competitions/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("competition", competitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Соревнование не найдено: " + id)));
        return "competitions/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Competition competition,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "competitions/form";
        competition.setId(id);
        competitionRepository.update(competition);
        redirect.addFlashAttribute("success", "Данные обновлены");
        return "redirect:/competitions/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        competitionRepository.delete(id);
        redirect.addFlashAttribute("success", "Соревнование удалено");
        return "redirect:/competitions";
    }
}
