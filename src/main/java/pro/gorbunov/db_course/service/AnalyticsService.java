package pro.gorbunov.db_course.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.model.AthleteRef;
import pro.gorbunov.db_course.model.CorrelationRow;
import pro.gorbunov.db_course.model.Forecast;
import pro.gorbunov.db_course.model.ForecastRow;
import pro.gorbunov.db_course.model.RankCount;
import pro.gorbunov.db_course.repository.AnalyticsRepository;
import pro.gorbunov.db_course.repository.AthleteProgressRepository;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final AthleteProgressRepository progressRepository;

    public List<ForecastRow> forecasts() {
        return analyticsRepository.findAthletesWithJumps().stream()
                .map(this::withForecast)
                .filter(row -> row.forecastDistance() != null)
                .sorted(Comparator.comparing(ForecastRow::trendPerYear).reversed())
                .toList();
    }

    public List<CorrelationRow> correlations() {
        return analyticsRepository.findCorrelations();
    }

    public List<RankCount> rankDistribution() {
        return analyticsRepository.findRankDistribution();
    }

    public Map<String, Object> trendChart(List<ForecastRow> forecasts) {
        return barChart("Прирост, м/год", "м/год", forecasts.stream().map(ForecastRow::athleteName).toList(),
                dataset("Тренд за год", forecasts, ForecastRow::trendPerYear));
    }

    public Map<String, Object> correlationChart(List<CorrelationRow> correlations) {
        return barChart("Коэффициент корреляции", "", correlations.stream().map(CorrelationRow::hillType).toList(),
                dataset("Рост", correlations, CorrelationRow::heightCorrelation),
                dataset("Вес", correlations, CorrelationRow::weightCorrelation));
    }

    public Map<String, Object> rankChart(List<RankCount> distribution) {
        return barChart("Спортсменов", "чел.", distribution.stream().map(RankCount::rankName).toList(),
                dataset("Спортсменов", distribution, RankCount::athletes));
    }

    private ForecastRow withForecast(AthleteRef athlete) {
        Forecast forecast = progressRepository.forecast(athlete.athleteId(), AthleteProgressService.HORIZON_DAYS);
        return new ForecastRow(athlete.athleteId(), athlete.athleteName(), athlete.rankName(), forecast.forecastDistance(),
                forecast.trendPerYear(), forecast.jumpsUsed(), forecast.rSquared());
    }

    @SafeVarargs
    private Map<String, Object> barChart(String yLabel, String unit, List<String> labels, Map<String, Object>... datasets) {
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "bar");
        chart.put("labels", labels);
        chart.put("yLabel", yLabel);
        chart.put("unit", unit);
        chart.put("datasets", List.of(datasets));
        return chart;
    }

    private <T> Map<String, Object> dataset(String label, List<T> rows, Function<T, ? extends Number> value) {
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", label);
        dataset.put("data", rows.stream().map(value).map(number -> number == null ? null : new BigDecimal(number.toString())).toList());
        return dataset;
    }
}
