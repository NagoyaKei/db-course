package pro.gorbunov.db_course.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AthleteStatus implements Badge {
    ACTIVE("Активен", "success", "bi-check-circle"),
    INACTIVE("Неактивен", "secondary", "bi-dash-circle"),
    SUSPENDED("Дисквалифицирован", "danger", "bi-slash-circle");

    private final String label;
    private final String tone;
    private final String icon;
}
