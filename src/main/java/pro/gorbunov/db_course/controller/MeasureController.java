package pro.gorbunov.db_course.controller;

import java.time.LocalDate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
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
import pro.gorbunov.db_course.model.Athlete;
import pro.gorbunov.db_course.model.PhysiologicalRecord;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.PhysiologicalRecordRepository;

@Controller
@RequestMapping("/athletes/{athleteId}/measures")
@RequiredArgsConstructor
public class MeasureController {

    private final PhysiologicalRecordRepository measureRepository;
    private final AthleteRepository athleteRepository;

    @GetMapping("/new")
    public String newForm(@PathVariable Long athleteId, Model model) {
        PhysiologicalRecord measure = new PhysiologicalRecord();
        measure.setMeasureDate(LocalDate.now());
        return form(athleteId, measure, model);
    }

    @PostMapping
    public String create(@PathVariable Long athleteId, @Valid @ModelAttribute("measure") PhysiologicalRecord measure,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        measure.setAthleteId(athleteId);
        return save(measure, errors, model, redirect, "Замер добавлен");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long athleteId, @PathVariable Long id, Model model) {
        return form(athleteId, findOwned(athleteId, id), model);
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long athleteId, @PathVariable Long id, @Valid @ModelAttribute("measure") PhysiologicalRecord measure,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        findOwned(athleteId, id);
        measure.setAthleteId(athleteId);
        measure.setRecordId(id);
        return save(measure, errors, model, redirect, "Замер обновлен");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long athleteId, @PathVariable Long id, RedirectAttributes redirect) {
        measureRepository.delete(findOwned(athleteId, id));
        redirect.addFlashAttribute("success", "Замер удален");
        return "redirect:/athletes/" + athleteId;
    }

    private String form(Long athleteId, PhysiologicalRecord measure, Model model) {
        Athlete athlete = athleteRepository.findById(athleteId).orElseThrow(NotFoundException::new);
        model.addAttribute("athlete", athlete);
        model.addAttribute("measure", measure);
        return "measures/form";
    }

    private PhysiologicalRecord findOwned(Long athleteId, Long id) {
        return measureRepository.findById(id)
                .filter(measure -> measure.getAthleteId().equals(athleteId))
                .orElseThrow(NotFoundException::new);
    }

    private String save(PhysiologicalRecord measure, BindingResult errors, Model model, RedirectAttributes redirect, String message) {
        if (!errors.hasErrors()) {
            rejectOnConflict(measure, errors);
        }
        if (errors.hasErrors()) {
            return form(measure.getAthleteId(), measure, model);
        }
        redirect.addFlashAttribute("success", message);
        return "redirect:/athletes/" + measure.getAthleteId();
    }

    private void rejectOnConflict(PhysiologicalRecord measure, BindingResult errors) {
        try {
            measureRepository.save(measure);
        } catch (DbActionExecutionException e) {
            if (e.getCause() instanceof DuplicateKeyException) {
                errors.rejectValue("measureDate", "duplicate", "Замер на эту дату уже есть");
            } else if (e.getCause() instanceof DataIntegrityViolationException) {
                errors.rejectValue("measureDate", "invalid", "Дата замера не может быть раньше даты рождения");
            } else {
                throw e;
            }
        }
    }
}
