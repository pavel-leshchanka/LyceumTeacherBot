package by.faeton.lyceumteacherbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    public static final String LABORATORY_NOTEBOOK_COLUMN = "C";
    public static final String TEST_NOTEBOOK_COLUMN = "D";
    public static final String START_MARKS_COLUMN = "K";
    public static final String END_MARKS_COLUMN = "BZ";
    public static final String DATE_FIELD = "2";
    public static final String FIELD_TYPE_OF_WORK = "3";


    private String id;
    private String list;
    private String field;

    public String getLaboratoryNotebookColumn() {
        return LABORATORY_NOTEBOOK_COLUMN + field;
    }

    public String getTestNotebookColumn() {
        return TEST_NOTEBOOK_COLUMN + field;
    }

    public String getMarksColumn() {
        return getColumn(field);
    }

    public String getDateColumn() {
        return getColumn(DATE_FIELD);
    }

    public String getTypeOfWorkColumn() {
        return getColumn(FIELD_TYPE_OF_WORK);
    }

    private String getColumn(String field) {
        return START_MARKS_COLUMN
                + field
                + ':'
                + END_MARKS_COLUMN
                + field;
    }
}
