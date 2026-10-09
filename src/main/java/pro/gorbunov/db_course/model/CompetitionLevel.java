package pro.gorbunov.db_course.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompetitionLevel implements Badge {
    LOCAL("Местный", "secondary", "bi-geo-alt"),
    REGIONAL("Региональный", "primary", "bi-map"),
    NATIONAL("Национальный", "warning", "bi-flag"),
    INTERNATIONAL("Международный", "danger", "bi-globe2");

    private final String label;
    private final String tone;
    private final String icon;
}
