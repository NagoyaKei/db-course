package pro.gorbunov.db_course.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.repository.NormRepository;
import pro.gorbunov.db_course.service.HillSelector;
import pro.gorbunov.db_course.service.NormMatrixService;

@Controller
@RequestMapping("/norms")
public class NormController {

    private static final String DEFAULT_GENDER = "M";

    private final NormRepository normRepository;
    private final HillRepository hillRepository;
    private final NormMatrixService normMatrixService;
    private final HillSelector hillSelector;

    public NormController(NormRepository normRepository, HillRepository hillRepository, NormMatrixService normMatrixService,
                          HillSelector hillSelector) {
        this.normRepository = normRepository;
        this.hillRepository = hillRepository;
        this.normMatrixService = normMatrixService;
        this.hillSelector = hillSelector;
    }

    @GetMapping
    public String norms(@RequestParam(required = false) String hill, @RequestParam(required = false) String gender, Model model) {
        List<Hill> hills = hillRepository.findAll();
        Hill selectedHill = hillSelector.select(hills, hill);
        String selectedGender = "F".equals(gender) ? "F" : DEFAULT_GENDER;

        model.addAttribute("hills", hills);
        model.addAttribute("selectedHill", selectedHill);
        model.addAttribute("selectedGender", selectedGender);
        if (selectedHill != null) {
            model.addAttribute("matrix", normMatrixService.build(normRepository.findByHillAndGender(selectedHill.getType(), selectedGender)));
        }
        return "norms/index";
    }
}
