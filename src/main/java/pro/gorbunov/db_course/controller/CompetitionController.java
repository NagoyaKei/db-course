package pro.gorbunov.db_course.controller;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.exception.NotFoundException;
import pro.gorbunov.db_course.exception.ResultsRejectedException;
import pro.gorbunov.db_course.model.Competition;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.model.ProtocolRow;
import pro.gorbunov.db_course.model.ResultsForm;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CompetitionRepository;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.JumpResultRepository;
import pro.gorbunov.db_course.service.CompetitionProtocolService;

@Controller
@RequestMapping("/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionRepository competitionRepository;
    private final JumpResultRepository jumpResultRepository;
    private final HillRepository hillRepository;
    private final AthleteRepository athleteRepository;
    private final CompetitionProtocolService protocolService;
    private final ObjectMapper objectMapper;

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
    public String detail(@PathVariable Long id, Model model) throws JsonProcessingException {
        Competition competition = competitionRepository.findById(id).orElseThrow(NotFoundException::new);
        List<ProtocolRow> protocol = protocolService.protocol(id);
        model.addAttribute("competition", competition);
        model.addAttribute("results", jumpResultRepository.findByCompetition(id));
        model.addAttribute("protocol", protocol);
        model.addAttribute("chart", objectMapper.writeValueAsString(protocolService.chart(protocol)));
        if (competition.getStatus().isAcceptingResults()) {
            model.addAttribute("athletes", athleteRepository.findAll());
            model.addAttribute("resultsForm", new ResultsForm());
        }
        return "competitions/detail";
    }

    @PostMapping("/{id}/results")
    public String registerResults(@PathVariable Long id, @ModelAttribute ResultsForm form, RedirectAttributes redirect) {
        try {
            int saved = protocolService.register(id, form);
            redirect.addFlashAttribute("success", "Результатов зарегистрировано: " + saved);
        } catch (ResultsRejectedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/competitions/" + id;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("competition", new Competition());
        return "competitions/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Competition competition,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "competitions/form";
        Long id = competitionRepository.save(competition).getId();
        redirect.addFlashAttribute("success", "Соревнование добавлено");
        return "redirect:/competitions/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("competition", competitionRepository.findById(id)
                .orElseThrow(NotFoundException::new));
        return "competitions/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Competition competition,
                         BindingResult errors,
                         RedirectAttributes redirect) {
        if (errors.hasErrors()) return "competitions/form";
        competition.setId(id);
        competitionRepository.save(competition);
        redirect.addFlashAttribute("success", "Данные обновлены");
        return "redirect:/competitions/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        competitionRepository.deleteById(id);
        redirect.addFlashAttribute("success", "Соревнование удалено");
        return "redirect:/competitions";
    }
}
