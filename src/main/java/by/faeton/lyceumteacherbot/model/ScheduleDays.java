package by.faeton.lyceumteacherbot.model;

import lombok.Getter;

@Getter
public enum ScheduleDays {
    MONDAY("Понедельник"),
    TUESDAY("Вторник"),
    WEDNESDAY("Среда"),
    THURSDAY("Четверг"),
    FRIDAY("Пятница"),
    SATURDAY("Суббота"),
    ALL("Неделя"),
    TODAY("Сегодня");

    private final String dayName;

    ScheduleDays(String string) {
        this.dayName = string;
    }
}
