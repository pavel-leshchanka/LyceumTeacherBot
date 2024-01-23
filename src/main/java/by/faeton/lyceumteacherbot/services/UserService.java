package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class UserService {

    private String laboratoryNotebookColumn;
    private String testNotebookColumn;
    private String startMarksColumn;
    private String endMarksColumn;
    private String dateField;
    private String fieldTypeOfWork;
    private String startQuarterMarksColumn;
    private String endQuarterMarksColumn;

    public String getLaboratoryNotebookColumn(User user) {
        return laboratoryNotebookColumn + user.getField();
    }

    public String getTestNotebookColumn(User user) {
        return testNotebookColumn + user.getField();
    }

    public String getMarksColumn(User user) {
        return getColumn(user.getField(), startMarksColumn, endMarksColumn);
    }

    public String getQuarterMarksColumn(User user) {
        return getColumn(user.getField(), startQuarterMarksColumn, endQuarterMarksColumn);
    }

    public String getDateColumn() {
        return getColumn(dateField, startMarksColumn, endMarksColumn);
    }

    public String getQuarterNameColumn() {
        return getColumn(dateField, startQuarterMarksColumn, endQuarterMarksColumn);
    }

    public String getTypeOfWorkColumn() {
        return getColumn(fieldTypeOfWork, startMarksColumn, endMarksColumn);
    }

    public String getTypeOfQuarterColumn() {
        return getColumn(fieldTypeOfWork, startQuarterMarksColumn, endQuarterMarksColumn);
    }

    private String getColumn(String field, String startColumn, String endColumn) {
        return startColumn
                + field
                + ':'
                + endColumn
                + field;
    }
}
