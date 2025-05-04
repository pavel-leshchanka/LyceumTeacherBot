package by.faeton.lyceumteacherbot.model;

import lombok.Getter;

@Getter
public enum Semester {
    FIRST("Первый", 1),
    SECOND("Второй", 2);

    private final String semesterName;
    private final int semesterNumber;

    Semester(String semesterName, int semesterNumber) {
        this.semesterName = semesterName;
        this.semesterNumber = semesterNumber;
    }
}
