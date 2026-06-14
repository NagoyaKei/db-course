package pro.gorbunov.db_course.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.model.Athlete;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.SportRankRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/athletes")
public class AthleteController {

    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final SportRankRepository sportRankRepository;

    public AthleteController(AthleteRepository athleteRepository,
                             CoachRepository coachRepository,
                             SportRankRepository sportRankRepository) {
        this.athleteRepository = athleteRepository;
        this.coachRepository = coachRepository;
        this.sportRankRepository = sportRankRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("athletes",
                status != null && !status.isBlank()
                        ? athleteRepository.findByStatus(status)
                        : athleteRepository.findAll()
        );
        model.addAttribute("selectedStatus", status);
        return "athletes/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Athlete athlete = athleteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Спортсмен не найден: " + id));
        model.addAttribute("athlete", athlete);
        return "athletes/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("athlete", new Athlete());
        model.addAttribute("coaches", coachRepository.findAll());
        model.addAttribute("ranks", sportRankRepository.findAll());
        return "athletes/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Athlete athlete,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("coaches", coachRepository.findAll());
            model.addAttribute("ranks", sportRankRepository.findAll());
            return "athletes/form";
        }
        Long id = athleteRepository.create(athlete);
        redirect.addFlashAttribute("success", "Спортсмен успешно добавлен");
        return "redirect:/athletes/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("athlete", athleteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Спортсмен не найден: " + id)));
        model.addAttribute("coaches", coachRepository.findAll());
        model.addAttribute("ranks", sportRankRepository.findAll());
        return "athletes/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Athlete athlete,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("coaches", coachRepository.findAll());
            model.addAttribute("ranks", sportRankRepository.findAll());
            return "athletes/form";
        }
        athlete.setPersonId(id);
        athleteRepository.update(athlete);
        redirect.addFlashAttribute("success", "Данные обновлены");
        return "redirect:/athletes/" + id;
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        athleteRepository.deactivate(id);
        redirect.addFlashAttribute("success", "Спортсмен деактивирован");
        return "redirect:/athletes";
    }
}
