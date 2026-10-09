package pro.gorbunov.db_course.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.model.Forecast;
import pro.gorbunov.db_course.model.JumpDynamics;
import pro.gorbunov.db_course.repository.AthleteProgressRepository;

@Service
@RequiredArgsConstructor
public class AthleteProgressService {

    public static final int HORIZON_DAYS = 90;

    private static final List<Integer> CHART_HORIZONS = List.of(30, 60, 90);

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AthleteProgressRepository progressRepository;

    public Forecast forecast(Long athleteId) {
        Forecast forecast = progressRepository.forecast(athleteId, HORIZON_DAYS);
        return forecast.forecastDistance() == null ? null : forecast;
    }

    public Map<String, Object> chart(Long athleteId, boolean withForecast) {
        List<JumpDynamics> dynamics = progressRepository.findDynamicsOnLastHill(athleteId);
        if (dynamics.isEmpty()) {
            return null;
        }
        List<String> labels = new ArrayList<>(dynamics.stream().map(jump -> LABEL_FORMAT.format(jump.jumpDate())).toList());
        List<BigDecimal> distances = column(dynamics, JumpDynamics::distance);
        List<BigDecimal> averages = column(dynamics, JumpDynamics::movingAverage);
        List<BigDecimal> forecasts = column(dynamics, jump -> null);
        if (withForecast) {
            LocalDate lastDate = dynamics.get(dynamics.size() - 1).jumpDate();
            forecasts.set(forecasts.size() - 1, distances.get(dynamics.size() - 1));
            for (int days : CHART_HORIZONS) {
                labels.add(LABEL_FORMAT.format(lastDate.plusDays(days)));
                distances.add(null);
                averages.add(null);
                forecasts.add(progressRepository.forecast(athleteId, days).forecastDistance());
            }
        }
        return chartSpec(dynamics.get(0).hillType(), labels, distances, averages, forecasts);
    }

    @SafeVarargs
    private void appendEmptySlot(List<String> labels, List<BigDecimal>... columns) {
        labels.add("");
        for (List<BigDecimal> column : columns) {
            column.add(null);
        }
    }

    private List<BigDecimal> column(List<JumpDynamics> dynamics, Function<JumpDynamics, BigDecimal> getter) {
        return new ArrayList<>(dynamics.stream().map(getter).toList());
    }

    private Map<String, Object> chartSpec(String hillType, List<String> labels, List<BigDecimal> distances, List<BigDecimal> averages,
                                          List<BigDecimal> forecasts) {
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "line");
        chart.put("labels", labels);
        chart.put("yLabel", "Дистанция, м (" + hillType + ")");
        chart.put("datasets", List.of(
                dataset("Результат", distances, false),
                dataset("Среднее за 3 прыжка", averages, false),
                dataset("Прогноз", forecasts, true)));
        return chart;
    }

    private Map<String, Object> dataset(String label, List<BigDecimal> data, boolean dashed) {
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", label);
        dataset.put("data", data);
        dataset.put("dashed", dashed);
        return dataset;
    }
}
