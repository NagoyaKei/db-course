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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pro.gorbunov.db_course.exception.NotFoundException;
import pro.gorbunov.db_course.model.JumpResult;
import pro.gorbunov.db_course.model.Record;
import pro.gorbunov.db_course.repository.JumpResultRepository;
import pro.gorbunov.db_course.repository.RecordRepository;

@Controller
@RequestMapping("/jumps/{jumpId}/record")
@RequiredArgsConstructor
public class JumpRecordController {

    private final RecordRepository recordRepository;
    private final JumpResultRepository jumpResultRepository;

    @GetMapping
    public String form(@PathVariable Long jumpId, Model model) {
        Record record = recordRepository.findByJumpId(jumpId).orElseGet(() -> newRecord(jumpId));
        return showForm(jumpId, record, model);
    }

    @PostMapping
    public String save(@PathVariable Long jumpId, @Valid @ModelAttribute("record") Record record, BindingResult errors, Model model,
                       RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            return showForm(jumpId, record, model);
        }
        record.setJumpId(jumpId);
        record.setRecordId(recordRepository.findByJumpId(jumpId).map(Record::getRecordId).orElse(null));
        recordRepository.save(record);
        redirect.addFlashAttribute("success", "Рекорд зафиксирован");
        return "redirect:/jumps";
    }

    @PostMapping("/delete")
    public String delete(@PathVariable Long jumpId, RedirectAttributes redirect) {
        recordRepository.findByJumpId(jumpId).ifPresent(recordRepository::delete);
        redirect.addFlashAttribute("success", "Отметка о рекорде снята");
        return "redirect:/jumps";
    }

    private Record newRecord(Long jumpId) {
        Record record = new Record();
        record.setJumpId(jumpId);
        return record;
    }

    private String showForm(Long jumpId, Record record, Model model) {
        JumpResult jump = jumpResultRepository.findById(jumpId).orElseThrow(NotFoundException::new);
        model.addAttribute("jump", jump);
        model.addAttribute("record", record);
        return "records/form";
    }
}
