package pro.gorbunov.db_course.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.HillRepository;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/hills")
public class HillController {

    private static final Pattern TYPE_PATTERN = Pattern.compile("^[A-Z0-9_-]{1,20}$");
    private static final String FORM_VIEW = "hills/form";
    private static final String LIST_REDIRECT = "redirect:/hills";

    private final HillRepository hillRepository;

    public HillController(HillRepository hillRepository) {
        this.hillRepository = hillRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hills", hillRepository.findAll());
        return "hills/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("hill", new Hill());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@ModelAttribute Hill hill, BindingResult errors, RedirectAttributes redirect) {
        validate(hill, errors);
        if (errors.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            hillRepository.create(hill);
        } catch (DuplicateKeyException e) {
            rejectDuplicateType(errors);
            return FORM_VIEW;
        }
        redirect.addFlashAttribute("success", "Трамплин добавлен");
        return LIST_REDIRECT;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("hill", hillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Трамплин не найден: " + id)));
        return FORM_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Hill hill, BindingResult errors, RedirectAttributes redirect) {
        hill.setId(id);
        validate(hill, errors);
        if (errors.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            hillRepository.update(hill);
        } catch (DuplicateKeyException e) {
            rejectDuplicateType(errors);
            return FORM_VIEW;
        }
        redirect.addFlashAttribute("success", "Данные трамплина обновлены");
        return LIST_REDIRECT;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            hillRepository.delete(id);
            redirect.addFlashAttribute("success", "Трамплин удален");
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("error", "Трамплин используется в соревнованиях или нормативах, удалить нельзя");
        }
        return LIST_REDIRECT;
    }

    private void rejectDuplicateType(BindingResult errors) {
        errors.rejectValue("type", "duplicate", "Трамплин с таким кодом уже существует");
    }

    private void validate(Hill hill, BindingResult errors) {
        if (hill.getType() == null || !TYPE_PATTERN.matcher(hill.getType()).matches()) {
            errors.rejectValue("type", "invalid", "Код: латинские заглавные буквы, цифры, - и _, до 20 символов");
        }
        if (hill.getName() == null || hill.getName().isBlank() || hill.getName().length() > 100) {
            errors.rejectValue("name", "invalid", "Название обязательно, до 100 символов");
        }
        if (hill.getKpoint() == null || hill.getKpoint() < 1) {
            errors.rejectValue("kpoint", "invalid", "K-точка должна быть больше 0");
        }
        if (hill.getMeterValue() == null || hill.getMeterValue().compareTo(BigDecimal.ZERO) <= 0) {
            errors.rejectValue("meterValue", "invalid", "Очки за метр должны быть больше 0");
        }
    }
}
