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
import pro.gorbunov.db_course.model.Coach;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/coaches")
public class CoachController {

    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;

    public CoachController(CoachRepository coachRepository,
                           AthleteRepository athleteRepository) {
        this.coachRepository = coachRepository;
        this.athleteRepository = athleteRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coaches", coachRepository.findAll());
        return "coaches/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("coach", coachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тренер не найден: " + id)));
        model.addAttribute("athletes", athleteRepository.findByCoach(id));
        return "coaches/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("coach", new Coach());
        return "coaches/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Coach coach,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "coaches/form";
        Long id = coachRepository.create(coach);
        redirect.addFlashAttribute("success", "Тренер успешно добавлен");
        return "redirect:/coaches/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("coach", coachRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Тренер не найден: " + id)));
        return "coaches/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Coach coach,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "coaches/form";
        coach.setPersonId(id);
        coachRepository.update(coach);
        redirect.addFlashAttribute("success", "Данные обновлены");
        return "redirect:/coaches/" + id;
    }
}
