package pro.gorbunov.db_course.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pro.gorbunov.db_course.model.NormRow;

@Service
public class NormMatrixService {

    public record Column(String type, String name, Integer minAge, Integer maxAge) {
    }

    public record Cell(BigDecimal distance, BigDecimal heat) {
    }

    public record Row(String rankType, String rankName, List<Cell> cells) {
    }

    public record Matrix(List<Column> columns, List<Row> rows) {
    }

    public Matrix build(List<NormRow> norms) {
        Map<String, Column> columns = collectColumns(norms);
        Map<String, NormRow> ranks = collectRanks(norms);
        Map<String, BigDecimal> distances = indexDistances(norms);
        BigDecimal min = norms.stream().map(NormRow::minDistance).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = norms.stream().map(NormRow::minDistance).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        List<Row> rows = new ArrayList<>();
        for (NormRow rank : ranks.values()) {
            rows.add(new Row(rank.rankType(), rank.rankName(), buildCells(rank, columns, distances, min, max)));
        }
        return new Matrix(new ArrayList<>(columns.values()), rows);
    }

    private List<Cell> buildCells(NormRow rank, Map<String, Column> columns, Map<String, BigDecimal> distances, BigDecimal min, BigDecimal max) {
        List<Cell> cells = new ArrayList<>();
        for (Column column : columns.values()) {
            BigDecimal distance = distances.get(key(rank.rankType(), column.type()));
            cells.add(new Cell(distance, distance == null ? null : heat(distance, min, max)));
        }
        return cells;
    }

    private BigDecimal heat(BigDecimal distance, BigDecimal min, BigDecimal max) {
        if (max.compareTo(min) == 0) {
            return new BigDecimal("0.5");
        }
        return distance.subtract(min).divide(max.subtract(min), 3, RoundingMode.HALF_UP);
    }

    private Map<String, Column> collectColumns(List<NormRow> norms) {
        Map<String, Column> columns = new LinkedHashMap<>();
        for (NormRow norm : norms) {
            columns.putIfAbsent(norm.ageCategoryType(),
                    new Column(norm.ageCategoryType(), norm.ageCategoryName(), norm.minAge(), norm.maxAge()));
        }
        return columns;
    }

    private Map<String, NormRow> collectRanks(List<NormRow> norms) {
        Map<String, NormRow> ranks = new LinkedHashMap<>();
        norms.stream()
                .sorted((a, b) -> Integer.compare(a.rankSortOrder(), b.rankSortOrder()))
                .forEach(norm -> ranks.putIfAbsent(norm.rankType(), norm));
        return ranks;
    }

    private Map<String, BigDecimal> indexDistances(List<NormRow> norms) {
        Map<String, BigDecimal> distances = new HashMap<>();
        for (NormRow norm : norms) {
            distances.put(key(norm.rankType(), norm.ageCategoryType()), norm.minDistance());
        }
        return distances;
    }

    private String key(String rankType, String categoryType) {
        return rankType + "|" + categoryType;
    }
}
