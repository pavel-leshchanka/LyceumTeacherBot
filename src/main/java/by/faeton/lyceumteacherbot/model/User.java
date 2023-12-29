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
    public static final String START_QUARTER_MARKS_COLUMN = "E";
    public static final String END_QUARTER_MARKS_COLUMN = "J";


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
        return getMarksColumn(field);
    }

    public String getQuarterMarksColumn() {
        return getQuarterMarksColumn(field);
    }

    public String getDateColumn() {
        return getMarksColumn(DATE_FIELD);
    }

    public String getQuarterNameColumn() {
        return getQuarterMarksColumn(DATE_FIELD);
    }

    public String getTypeOfWorkColumn() {
        return getMarksColumn(FIELD_TYPE_OF_WORK);
    }

    public String getTypeOfQuarterColumn() {
        return getQuarterMarksColumn(FIELD_TYPE_OF_WORK);
    }

    private String getMarksColumn(String field) {
        return START_MARKS_COLUMN
                + field
                + ':'
                + END_MARKS_COLUMN
                + field;
    }

    private String getQuarterMarksColumn(String field) {
        return START_QUARTER_MARKS_COLUMN
                + field
                + ':'
                + END_QUARTER_MARKS_COLUMN
                + field;
    }
}
