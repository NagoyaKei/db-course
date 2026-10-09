package pro.gorbunov.db_course.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    M("Мужской", "Мужчины", "bi-gender-male"),
    F("Женский", "Женщины", "bi-gender-female");

    private final String label;
    private final String groupLabel;
    private final String icon;
}
