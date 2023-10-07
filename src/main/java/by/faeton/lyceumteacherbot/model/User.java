package by.faeton.lyceumteacherbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User {
    private static final String LABORATORY_NOTEBOOK_COLUMN = "C";
    private static final String TEST_NOTEBOOK_COLUMN = "D";
    private static final String START_MARKS_COLUMN = "F";
    private static final String END_MARKS_COLUMN = "BZ";

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
        return START_MARKS_COLUMN
                + field
                + ':'
                + END_MARKS_COLUMN
                + field;
    }
}
