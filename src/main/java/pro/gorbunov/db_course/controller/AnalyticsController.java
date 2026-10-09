package pro.gorbunov.db_course.controller;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pro.gorbunov.db_course.model.CorrelationRow;
import pro.gorbunov.db_course.model.ForecastRow;
import pro.gorbunov.db_course.model.RankCount;
import pro.gorbunov.db_course.service.AnalyticsService;
import pro.gorbunov.db_course.service.AthleteProgressService;

@Controller
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String analytics(Model model) throws JsonProcessingException {
        List<ForecastRow> forecasts = analyticsService.forecasts();
        List<CorrelationRow> correlations = analyticsService.correlations();
        List<RankCount> distribution = analyticsService.rankDistribution();

        model.addAttribute("forecasts", forecasts);
        model.addAttribute("horizonDays", AthleteProgressService.HORIZON_DAYS);
        model.addAttribute("correlations", correlations);
        model.addAttribute("distribution", distribution);
        model.addAttribute("trendChart", objectMapper.writeValueAsString(analyticsService.trendChart(forecasts)));
        model.addAttribute("correlationChart", objectMapper.writeValueAsString(analyticsService.correlationChart(correlations)));
        model.addAttribute("rankChart", objectMapper.writeValueAsString(analyticsService.rankChart(distribution)));
        return "analytics/index";
    }
}
