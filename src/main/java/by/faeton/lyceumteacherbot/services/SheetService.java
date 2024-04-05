package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;


@Service
@RequiredArgsConstructor
public class SheetService {
    private final SheetListener sheetListener;
    private final UserService userService;
    private final StudentService studentService;
    private final StudentsRepository studentsRepository;
    private HashMap<String, String> ass = new HashMap<>();

    {
        ass.put("с", "семейные обстоятельства");
        ass.put("б", "болезнь");
        ass.put("бу", "без уважительной");
        ass.put("о", "соревнования");
    }

    public String getStudentMarks(User user) {
        Optional<ArrayList<ArrayList<String>>> sheetDateLine = sheetListener.getSheetList(user.getList(), userService.getDateColumn());
        Optional<ArrayList<ArrayList<String>>> sheetTypeLine = sheetListener.getSheetList(user.getList(), userService.getTypeOfWorkColumn());
        Optional<ArrayList<ArrayList<String>>> sheetMarksLine = sheetListener.getSheetList(user.getList(), userService.getMarksColumn(user));
        String marks = linesToString(sheetDateLine, sheetTypeLine, sheetMarksLine);
        return marks;
    }

    public String getStudentQuarterMarks(User user) {
        Optional<ArrayList<ArrayList<String>>> sheetDateLine = sheetListener.getSheetList(user.getList(), userService.getQuarterNameColumn());
        Optional<ArrayList<ArrayList<String>>> sheetTypeLine = sheetListener.getSheetList(user.getList(), userService.getTypeOfQuarterColumn());
        Optional<ArrayList<ArrayList<String>>> sheetQuarterLine = sheetListener.getSheetList(user.getList(), userService.getQuarterMarksColumn(user));
        String marks = linesToString(sheetDateLine, sheetTypeLine, sheetQuarterLine);
        return marks;
    }

    public String getStudentLaboratoryNotebook(User user) {
        String field = userService.getLaboratoryNotebookColumn(user);
        String cell = sheetListener.getCell(user.getList(), field);
        return cell;
    }

    public String getStudentTestNotebook(User user) {
        String field = userService.getTestNotebookColumn(user);
        String cell = sheetListener.getCell(user.getList(), field);
        return cell;
    }

    public String getA() {
        List<Student> allStudents = studentsRepository.getAllStudents();
        Integer columnNumber = LocalDateTime.now().getDayOfMonth() * 8 - 7 + 2;
        Student student = new Student();
        student.setNumber(String.valueOf(1));
        String startCell = studentService.getStartCell(student, columnNumber);
        student.setNumber("30");
        String endCell = studentService.getStartCell(student, columnNumber + 7);
        Optional<ArrayList<ArrayList<String>>> sheetDateLine = sheetListener.getSheetList("Absenteeism", startCell + ":" + endCell);
        ArrayList<ArrayList<String>> arrayLists = sheetDateLine.get();
        String s = "10л";
        for (int i = 0; i < arrayLists.size(); i++) {
            if (arrayLists.get(i).size() > 0) {
                s += "\n" + allStudents.get(i).getName() + " ";//задал имя
                String s1 = arrayLists.get(i).get(0); //тип пропуска
                Integer start = 0;
                Integer end = 0;
                for (int j = 1; j < arrayLists.get(i).size(); j++) {
                    if (!arrayLists.get(i).get(j).equals(s1)) {
                        end = j - 1;
                        s += asdlk(start, end, s1);
                        s1 = arrayLists.get(i).get(j);
                        start = j;
                    }
                }
                if (end != arrayLists.get(i).size()) {
                    s += asdlk(start, arrayLists.get(i).size() - 1, s1);
                }
            }
        }
        return s;
    }

    private String asdlk(Integer start, Integer end, String type) {
        String ret = "";
        if (!type.equals("")) {
            if (start.equals(end)) {
                ret += start + " урок " + ass.get(type) + ". ";
            }
            if (!start.equals(end)) {
                ret += start + "-" + end + " уроки " + ass.get(type) + ". ";
            }
        } else {
            ret = "";
        }
        return ret;
    }

    public String linesToString(Optional<ArrayList<ArrayList<String>>>... values) {
        ArrayList<ArrayList<String>> returnedList = new ArrayList<>();
        for (Optional<ArrayList<ArrayList<String>>> firstValue : values) {
            firstValue.ifPresent(arrayLists -> returnedList.add(arrayLists.get(0)));
        }
        String returnedText = "";
        if (returnedList.size() > 1) {
            int lastNumberListOfValues = returnedList.size() - 1;
            ArrayList<String> lastList = returnedList.get(lastNumberListOfValues);
            int countElementsInLastListOfValues = lastList.size();
            for (int i = 0; i < countElementsInLastListOfValues; i++) {
                if (lastList.get(i) != null && !lastList.get(i).equals("")) {
                    for (ArrayList<String> strings : returnedList) {
                        if (i < strings.size()) {
                            returnedText = returnedText + strings.get(i) + " ";
                        }
                    }
                    returnedText += '\n';
                }
            }
        } else {
            returnedText = returnedText + NOT_AVAILABLE;
        }
        return returnedText;
    }

    public boolean writeAbsenteeism(DialogAttribute dialogAttribute) {
        ArrayList<String> receivedData = dialogAttribute.getReceivedData();
        Optional<Student> optionalStudent = studentsRepository.findByNumber(receivedData.get(0));
        if (optionalStudent.isPresent() && receivedData.size() == 4) {
            Student student = optionalStudent.get();
            int startOfAbsenteeism = Integer.parseInt(receivedData.get(1));
            int endOfAbsenteeism = Integer.parseInt(receivedData.get(2));
            String typeOfAbsenteeism = receivedData.get(3);
            List list = new ArrayList<>();
            if (endOfAbsenteeism >= startOfAbsenteeism) {
                for (int i = 0; i <= startOfAbsenteeism; i++) {
                    list.add(i, null);
                }
                for (int i = startOfAbsenteeism; i <= endOfAbsenteeism; i++) {
                    list.add(i, typeOfAbsenteeism);
                }
            }
            List<List<Object>> arrayLists = Arrays.asList(list);
            Integer columnNumber = LocalDateTime.now().getDayOfMonth() * 8 - 7 + 2 + startOfAbsenteeism;
            String startCell = studentService.getStartCell(student, columnNumber);
            sheetListener.writeSheet("Absenteeism", startCell, arrayLists);//todo list name
            return true;
        }
        return false;
    }
}
