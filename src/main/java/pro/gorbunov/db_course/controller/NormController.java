package pro.gorbunov.db_course.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.gorbunov.db_course.model.Gender;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.NormRepository;
import pro.gorbunov.db_course.service.HillSelector;
import pro.gorbunov.db_course.service.NormMatrixService;

@Controller
@RequestMapping("/norms")
@RequiredArgsConstructor
public class NormController {

    private final NormRepository normRepository;
    private final HillRepository hillRepository;
    private final NormMatrixService normMatrixService;
    private final HillSelector hillSelector;

    @GetMapping
    public String norms(@RequestParam(required = false) String hill, @RequestParam(defaultValue = "M") Gender gender, Model model) {
        List<Hill> hills = hillRepository.findAll();
        Hill selectedHill = hillSelector.select(hills, hill);

        model.addAttribute("hills", hills);
        model.addAttribute("selectedHill", selectedHill);
        model.addAttribute("selectedGender", gender);
        if (selectedHill != null) {
            model.addAttribute("matrix", normMatrixService.build(normRepository.findByHillAndGender(selectedHill.getType(), gender)));
        }
        return "norms/index";
    }
}
