package by.faeton.lyceumteacherbot.model;

import by.faeton.lyceumteacherbot.utils.DefaultMessages;
import lombok.Getter;

@Getter
public enum Statement {
    FIRST_QUARTER(DefaultMessages.FIRST_QUARTER),
    SECOND_QUARTER(DefaultMessages.SECOND_QUARTER),
    THREE_QUARTER(DefaultMessages.THREE_QUARTER),
    FOUR_QUARTER(DefaultMessages.FOUR_QUARTER),
    YEAR(DefaultMessages.YEAR),
    EXAM(DefaultMessages.EXAM),
    FINAL(DefaultMessages.FINAL);

    Statement(String statementName) {
        this.statementName = statementName;
    }

    private final String statementName;
}
