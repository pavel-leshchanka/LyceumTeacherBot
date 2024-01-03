package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
        return getMarksColumn(user.getField());
    }

    public String getQuarterMarksColumn(User user) {
        return getQuarterMarksColumn(user.getField());
    }

    public String getDateColumn() {
        return getMarksColumn(dateField);
    }

    public String getQuarterNameColumn() {
        return getQuarterMarksColumn(dateField);
    }

    public String getTypeOfWorkColumn() {
        return getMarksColumn(fieldTypeOfWork);
    }

    public String getTypeOfQuarterColumn() {
        return getQuarterMarksColumn(fieldTypeOfWork);
    }

    private String getMarksColumn(String field) {
        return startMarksColumn
                + field
                + ':'
                + endMarksColumn
                + field;
    }

    private String getQuarterMarksColumn(String field) {
        return startQuarterMarksColumn
                + field
                + ':'
                + endQuarterMarksColumn
                + field;
    }

    @SneakyThrows
    @PostConstruct
    public void setUp() {
        Optional<ArrayList<ArrayList<String>>> values = sheetListener.getSheetList(botConfig.getSettingsList());
        if (values.isPresent()) {
            for (ArrayList<String> value : values.get()) {
                if (value.get(0).equals("laboratoryNotebookColumn")) {
                    laboratoryNotebookColumn = value.get(1);
                }
                if (value.get(0).equals("testNotebookColumn")) {
                    testNotebookColumn = value.get(1);
                }
                if (value.get(0).equals("startMarksColumn")) {
                    startMarksColumn = value.get(1);
                }
                if (value.get(0).equals("endMarksColumn")) {
                    endMarksColumn = value.get(1);
                }
                if (value.get(0).equals("dateField")) {
                    dateField = value.get(1);
                }
                if (value.get(0).equals("fieldTypeOfWork")) {
                    fieldTypeOfWork = value.get(1);
                }
                if (value.get(0).equals("startQuarterMarksColumn")) {
                    startQuarterMarksColumn = value.get(1);
                }
                if (value.get(0).equals("endQuarterMarksColumn")) {
                    endQuarterMarksColumn = value.get(1);
                }
            }
        }
    }
}
