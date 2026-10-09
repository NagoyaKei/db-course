package pro.gorbunov.db_course.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompetitionStatus implements Badge {
    PLANNED("Запланировано", "info", "bi-calendar-event"),
    ACTIVE("Идет", "success", "bi-play-circle"),
    FINISHED("Завершено", "secondary", "bi-check2-all"),
    CANCELLED("Отменено", "danger", "bi-x-circle");

    private final String label;
    private final String tone;
    private final String icon;

    public boolean isAcceptingResults() {
        return this == ACTIVE || this == FINISHED;
    }
}
