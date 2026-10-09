package pro.gorbunov.db_course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.HillRepository;

@Controller
@RequestMapping("/hills")
@RequiredArgsConstructor
public class HillController {

  private final HillRepository hillRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("hills", hillRepository.findAll());
        return "hills/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("hill", new Hill());
        return "hills/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Hill hill, BindingResult errors, RedirectAttributes redirect) {
        return save(hill, errors, redirect, "Трамплин добавлен");
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("hill", hillRepository.findById(id).orElseThrow(NotFoundException::new));
        return "hills/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Hill hill, BindingResult errors, RedirectAttributes redirect) {
        hill.setId(id);
        return save(hill, errors, redirect, "Данные трамплина обновлены");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            hillRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Трамплин удален");
        } catch (DbActionExecutionException e) {
            redirect.addFlashAttribute("error", "Трамплин используется в соревнованиях или нормативах, удалить нельзя");
        }
        return "redirect:/hills";
    }

    private String save(Hill hill, BindingResult errors, RedirectAttributes redirect, String message) {
        if (errors.hasErrors()) {
            return "hills/form";
        }
        try {
            hillRepository.save(hill);
        } catch (DbActionExecutionException e) {
            if (!(e.getCause() instanceof DuplicateKeyException)) {
                throw e;
            }
            errors.rejectValue("type", "duplicate", "Трамплин с таким кодом уже существует");
            return "hills/form";
        }
        redirect.addFlashAttribute("success", message);
        return "redirect:/hills";
    }
}
