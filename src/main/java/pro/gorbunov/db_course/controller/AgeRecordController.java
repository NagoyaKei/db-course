package pro.gorbunov.db_course.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pro.gorbunov.db_course.model.AgeRecord;
import pro.gorbunov.db_course.model.Hill;
import pro.gorbunov.db_course.repository.AgeRecordRepository;
import pro.gorbunov.db_course.repository.HillRepository;
import pro.gorbunov.db_course.service.HillSelector;

@Controller
@RequestMapping("/records")
public class AgeRecordController {

    private final AgeRecordRepository ageRecordRepository;
    private final HillRepository hillRepository;
    private final ObjectMapper objectMapper;
    private final HillSelector hillSelector;

    public AgeRecordController(AgeRecordRepository ageRecordRepository, HillRepository hillRepository, ObjectMapper objectMapper,
                               HillSelector hillSelector) {
        this.ageRecordRepository = ageRecordRepository;
        this.hillRepository = hillRepository;
        this.objectMapper = objectMapper;
        this.hillSelector = hillSelector;
    }

    @GetMapping
    public String records(@RequestParam(required = false) String hill, Model model) throws JsonProcessingException {
        List<Hill> hills = hillRepository.findAll();
        Hill selectedHill = hillSelector.select(hills, hill);

        model.addAttribute("hills", hills);
        model.addAttribute("selectedHill", selectedHill);
        if (selectedHill != null) {
            List<AgeRecord> records = ageRecordRepository.findByHill(selectedHill.getType());
            model.addAttribute("records", records);
            model.addAttribute("chart", objectMapper.writeValueAsString(buildChart(records)));
        }
        return "records/index";
    }

    private Map<String, Object> buildChart(List<AgeRecord> records) {
        List<String> categories = records.stream().map(AgeRecord::ageCategoryName).distinct().toList();
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "bar");
        chart.put("labels", categories);
        chart.put("yLabel", "Дистанция, м");
        chart.put("datasets", List.of(dataset("Мужчины", "M", categories, records), dataset("Женщины", "F", categories, records)));
        return chart;
    }

    private Map<String, Object> dataset(String label, String gender, List<String> categories, List<AgeRecord> records) {
        List<BigDecimal> data = new ArrayList<>();
        for (String category : categories) {
            data.add(records.stream()
                    .filter(record -> record.gender().equals(gender) && record.ageCategoryName().equals(category))
                    .map(AgeRecord::distance)
                    .findFirst()
                    .orElse(null));
        }
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", label);
        dataset.put("data", data);
        return dataset;
    }
}
