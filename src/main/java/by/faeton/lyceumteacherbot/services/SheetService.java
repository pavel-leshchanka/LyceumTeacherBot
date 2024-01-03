package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SheetService {

    private final SheetListener sheetListener;
    private final UserService userService;

    public String getStudentMarks(User user) {
        Optional<ArrayList<ArrayList<String>>> sheetLine = sheetListener.getSheetList(user.getList(), userService.getMarksColumn(user));
        Optional<ArrayList<ArrayList<String>>> sheetDateLine = sheetListener.getSheetList(user.getList(), userService.getDateColumn());
        Optional<ArrayList<ArrayList<String>>> sheetTypeLine = sheetListener.getSheetList(user.getList(), userService.getTypeOfWorkColumn());
        String marks = getMarksLine(sheetDateLine, sheetTypeLine, sheetLine);

        return marks;
    }

    public String getStudentQuarterMarks(User user) {
        Optional<ArrayList<ArrayList<String>>> sheetLine = sheetListener.getSheetList(user.getList(), userService.getQuarterMarksColumn(user));
        Optional<ArrayList<ArrayList<String>>> sheetDateLine = sheetListener.getSheetList(user.getList(), userService.getQuarterNameColumn());
        Optional<ArrayList<ArrayList<String>>> sheetTypeLine = sheetListener.getSheetList(user.getList(), userService.getTypeOfQuarterColumn());
        String marks = getMarksLine(sheetDateLine, sheetTypeLine, sheetLine);

        return marks;
    }

    public String getStudentLaboratoryNotebook(User user) {
        String field = userService.getLaboratoryNotebookColumn(user);
        Optional<ArrayList<ArrayList<String>>> sheetJSONLine = sheetListener.getSheetList(user.getList(), field);
        if (sheetJSONLine.isPresent()) {
            return sheetJSONLine.get().get(0).get(0);
        } else {
            return "";
        }
    }

    public String getStudentTestNotebook(User user) {
        String field = userService.getTestNotebookColumn(user);
        Optional<ArrayList<ArrayList<String>>> sheetJSONLine = sheetListener.getSheetList(user.getList(), field);
        if (sheetJSONLine.isPresent()) {
            return sheetJSONLine.get().get(0).get(0);
        } else {
            return "";
        }
    }

    public String getMarksLine(Optional<ArrayList<ArrayList<String>>> dateValues,
                               Optional<ArrayList<ArrayList<String>>> typeValues,
                               Optional<ArrayList<ArrayList<String>>> textValues) {
        String returnedText = "";
        if (textValues.isPresent()) {
            ArrayList<String> dateLine = dateValues.get().get(0);
            ArrayList<String> typeLine = typeValues.get().get(0);
            ArrayList<String> textLine = textValues.get().get(0);//todo in method
            for (int i = 0; i < textLine.size(); i++) {
                if (textLine.get(i) != null && !textLine.get(i).equals("")) {
                    String date = "";
                    if (dateLine.size() > i && dateLine.get(i) != null) {
                        //todo check it
                        date = dateLine.get(i);
                    }
                    String type = "";
                    if (typeLine.size() > i && typeLine.get(i) != null) {
                        //todo check it
                        type = typeLine.get(i);
                    }
                    returnedText = returnedText + date + " " + type + " " + textLine.get(i) + '\n';
                }
            }
        }
        return returnedText;
    }

}
