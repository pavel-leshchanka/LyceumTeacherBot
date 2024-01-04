package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BotConfig botConfig;
    private final SheetListener sheetListener;

    public String laboratoryNotebookColumn;
    public String testNotebookColumn;
    public String startMarksColumn;
    public String endMarksColumn;
    public String dateField;
    public String fieldTypeOfWork;
    public String startQuarterMarksColumn;
    public String endQuarterMarksColumn;

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

    @PostConstruct
    public void setUp() {
        Optional<ArrayList<ArrayList<String>>> values = sheetListener.getSheetList(botConfig.getSettingsList());
        if (values.isPresent()) {
            for (ArrayList<String> value : values.get()) {
                switch (value.get(0)) {
                    case "laboratoryNotebookColumn" -> laboratoryNotebookColumn = value.get(1);
                    case "testNotebookColumn" -> testNotebookColumn = value.get(1);
                    case "startMarksColumn" -> startMarksColumn = value.get(1);
                    case "endMarksColumn" -> endMarksColumn = value.get(1);
                    case "dateField" -> dateField = value.get(1);
                    case "fieldTypeOfWork" -> fieldTypeOfWork = value.get(1);
                    case "startQuarterMarksColumn" -> startQuarterMarksColumn = value.get(1);
                    case "endQuarterMarksColumn" -> endQuarterMarksColumn = value.get(1);
                    default -> {
                        //todo log
                    }
                }
            }
        }
    }
}
