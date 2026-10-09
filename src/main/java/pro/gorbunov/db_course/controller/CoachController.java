package pro.gorbunov.db_course.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.exception.NotFoundException;
import pro.gorbunov.db_course.model.Coach;
import pro.gorbunov.db_course.model.StoredImage;
import pro.gorbunov.db_course.repository.AthleteRepository;
import pro.gorbunov.db_course.repository.CoachRepository;
import pro.gorbunov.db_course.repository.PersonImageRepository;
import pro.gorbunov.db_course.service.ImageUploadReader;
import pro.gorbunov.db_course.service.PersonPhotoService;

@Controller
@RequestMapping("/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;
    private final PersonImageRepository personImageRepository;
    private final ImageUploadReader imageUploadReader;
    private final PersonPhotoService personPhotoService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coaches", coachRepository.findAll());
        return "coaches/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("coach", coachRepository.findById(id)
                .orElseThrow(NotFoundException::new));
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
                         @RequestParam(required = false) MultipartFile photo,
                         RedirectAttributes redirect) {
        StoredImage storedPhoto = imageUploadReader.read(photo, errors);
        if (errors.hasErrors()) return "coaches/form";
        Long id = coachRepository.create(coach);
        personPhotoService.apply(id, storedPhoto, false);
        redirect.addFlashAttribute("success", "Тренер успешно добавлен");
        return "redirect:/coaches/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("coach", coachRepository.findById(id)
                .orElseThrow(NotFoundException::new));
        return "coaches/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Coach coach,
                         BindingResult errors,
                         @RequestParam(required = false) MultipartFile photo,
                         @RequestParam(defaultValue = "false") boolean removePhoto,
                         RedirectAttributes redirect) {
        StoredImage storedPhoto = imageUploadReader.read(photo, errors);
        coach.setPersonId(id);
        if (errors.hasErrors()) {
            coach.setHasPhoto(personImageRepository.exists(id));
            return "coaches/form";
        }
        coachRepository.update(coach);
        personPhotoService.apply(id, storedPhoto, removePhoto);
        redirect.addFlashAttribute("success", "Данные обновлены");
        return "redirect:/coaches/" + id;
    }
}
