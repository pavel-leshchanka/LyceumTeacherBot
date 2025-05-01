package by.faeton.lyceumteacherbot.model.lyceum;

import lombok.Getter;

@Getter
public enum Statement {
    FIRST_QUARTER("Первая четверть"),
    SECOND_QUARTER("Вторая четверть"),
    THREE_QUARTER("Третья четверть"),
    FOUR_QUARTER("Четвертая четверть"),
    YEAR("Годовая"),
    EXAM("Экзамен"),
    FINAL("Итоговая");

    Statement(String statementName) {
        this.statementName = statementName;
    }

    private final String statementName;
}
