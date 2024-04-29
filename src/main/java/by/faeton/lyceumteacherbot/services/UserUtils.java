package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUtils {

    private final FieldsNameConfig fieldsNameConfig;

    public String getNameOfCellUserLaboratoryNotebook(User user) {
        return fieldsNameConfig.laboratoryNotebookColumn() + user.getFieldOfSheetWithUser();
    }

    public String getNameOfCellUserTestNotebook(User user) {
        return fieldsNameConfig.testNotebookColumn() + user.getFieldOfSheetWithUser();
    }

    public String getCellsNameOfMarks(User user) {
        return getColumn(user.getFieldOfSheetWithUser(), fieldsNameConfig.startMarksColumn(), fieldsNameConfig.endMarksColumn());
    }

    public String getCellsNameOfQuarterMarks(User user) {
        return getColumn(user.getFieldOfSheetWithUser(), fieldsNameConfig.startQuarterMarksColumn(), fieldsNameConfig.endQuarterMarksColumn());
    }

    public String getCellsNameOfDate() {
        return getColumn(fieldsNameConfig.dateField(), fieldsNameConfig.startMarksColumn(), fieldsNameConfig.endMarksColumn());
    }

    public String getCellsNameOfQuarterName() {
        return getColumn(fieldsNameConfig.dateField(), fieldsNameConfig.startQuarterMarksColumn(), fieldsNameConfig.endQuarterMarksColumn());
    }

    public String getCellsNameOfTypeOfWork() {
        return getColumn(fieldsNameConfig.fieldTypeOfWork(), fieldsNameConfig.startMarksColumn(), fieldsNameConfig.endMarksColumn());
    }

    public String getCellsNameOfTypeOfQuarter() {
        return getColumn(fieldsNameConfig.fieldTypeOfWork(), fieldsNameConfig.startQuarterMarksColumn(), fieldsNameConfig.endQuarterMarksColumn());
    }

    private String getColumn(String field, String startColumn, String endColumn) {
        return startColumn
                + field
                + ':'
                + endColumn
                + field;
    }
}