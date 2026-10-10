package pro.gorbunov.db_course.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.exception.ResultsRejectedException;
import pro.gorbunov.db_course.model.ProtocolRow;
import pro.gorbunov.db_course.model.ResultEntry;
import pro.gorbunov.db_course.model.ResultsForm;
import pro.gorbunov.db_course.repository.ProtocolRepository;

@Service
@RequiredArgsConstructor
public class CompetitionProtocolService {

    private final ProtocolRepository protocolRepository;
    private final ObjectMapper objectMapper;

    public List<ProtocolRow> protocol(Long competitionId) {
        return protocolRepository.findByCompetition(competitionId);
    }

    public Map<String, Object> chart(List<ProtocolRow> protocol) {
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", "Сумма очков");
        dataset.put("data", protocol.stream().map(ProtocolRow::totalPoints).toList());
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "bar");
        chart.put("labels", protocol.stream().map(ProtocolRow::athleteName).toList());
        chart.put("yLabel", "Очки");
        chart.put("unit", "очк.");
        chart.put("datasets", List.of(dataset));
        return chart;
    }

    public int register(Long competitionId, ResultsForm form) {
        List<ResultEntry> entries = form.getEntries().stream().filter(entry -> !entry.isBlank()).toList();
        if (entries.isEmpty()) {
            throw new ResultsRejectedException("Заполните хотя бы одну строку");
        }
        rejectIncomplete(entries);
        try {
            protocolRepository.register(competitionId, toJson(entries));
        } catch (DataAccessException e) {
            throw new ResultsRejectedException("Результаты не сохранены: " + DbErrorMessages.firstLine(e));
        }
        return entries.size();
    }

    private void rejectIncomplete(List<ResultEntry> entries) {
        for (int index = 0; index < entries.size(); index++) {
            if (!entries.get(index).isComplete()) {
                throw new ResultsRejectedException("В строке " + (index + 1) + " укажите спортсмена и дистанцию");
            }
        }
    }

    private String toJson(List<ResultEntry> entries) {
        try {
            return objectMapper.writeValueAsString(entries.stream().map(this::toProcedureRow).toList());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, Object> toProcedureRow(ResultEntry entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("athlete_id", entry.getAthleteId());
        row.put("distance", entry.getDistance());
        row.put("style_points", entry.getStylePoints());
        row.put("wind_factor", entry.getWindFactor());
        row.put("attempt_number", entry.getAttemptNumber());
        return row;
    }
}
