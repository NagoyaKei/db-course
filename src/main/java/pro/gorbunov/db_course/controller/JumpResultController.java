package pro.gorbunov.db_course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.model.JumpResult;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/jumps")
public class JumpResultController {

    private final JumpResultRepository jumpResultRepository;
    private final AthleteRepository athleteRepository;
    private final CompetitionRepository competitionRepository;

    public JumpResultController(JumpResultRepository jumpResultRepository,
                                AthleteRepository athleteRepository,
                                CompetitionRepository competitionRepository) {
        this.jumpResultRepository = jumpResultRepository;
        this.athleteRepository = athleteRepository;
        this.competitionRepository = competitionRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("jumps", jumpResultRepository.findAll());
        return "jumps/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("jump", new JumpResult());
        model.addAttribute("athletes", athleteRepository.findAll());
        model.addAttribute("competitions", competitionRepository.findAll());
        return "jumps/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("jump") JumpResult jump,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("athletes", athleteRepository.findAll());
            model.addAttribute("competitions", competitionRepository.findAll());
            return "jumps/form";
        }
        jumpResultRepository.create(jump);
        redirect.addFlashAttribute("success", "Результат прыжка зарегистрирован");
        return "redirect:/jumps";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        jumpResultRepository.delete(id);
        redirect.addFlashAttribute("success", "Результат удален");
        return "redirect:/jumps";
    }
}
