package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;


@Service
@RequiredArgsConstructor
public class SheetService {
    private final SheetListener sheetListener;
    private final UserUtils userUtils;
    private final StudentService studentService;
    private final StudentsRepository studentsRepository;
    private final SheetListNameConfig sheetListNameConfig;
    private final FieldsNameConfig fieldsNameConfig;
    private final UserRepository userRepository;
    private final UserService userService;
    @Getter
    private final HashMap<String, String> typeAndValueOfAbsenteeism;

    public String getStudentMarks(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfDate());
        Optional<List<List<String>>> sheetTypeLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfTypeOfWork());
        Optional<List<List<String>>> sheetMarksLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfMarks(user));
        return linesToString(sheetDateLine, sheetTypeLine, sheetMarksLine);
    }

    public String getStudentQuarterMarks(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfQuarterName());
        Optional<List<List<String>>> sheetTypeLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfTypeOfQuarter());
        Optional<List<List<String>>> sheetQuarterLine = sheetListener.getSheetList(listOfGoogleSheet, userUtils.getCellsNameOfQuarterMarks(user));
        return linesToString(sheetDateLine, sheetTypeLine, sheetQuarterLine);
    }

    public String getStudentLaboratoryNotebook(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        String field = userUtils.getNameOfCellUserLaboratoryNotebook(user);
        return sheetListener.getCell(listOfGoogleSheet, field);
    }

    public String getStudentTestNotebook(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        String field = userUtils.getNameOfCellUserTestNotebook(user);
        return sheetListener.getCell(listOfGoogleSheet, field);
    }

    public String getTextOfAbsenteeism() {
        List<Student> allStudents = studentsRepository.getAllStudents();
        int columnNumber = LocalDateTime.now().getDayOfMonth() * 8 - 7 + 2;
        Student student = new Student();
        student.setStudentNumber(String.valueOf(1));
        String startCell = studentService.getNameStartCellOfAbsenteeism(student, columnNumber);
        student.setStudentNumber(String.valueOf(allStudents.size()));
        String endCell = studentService.getNameStartCellOfAbsenteeism(student, columnNumber + 7);
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetList(sheetListNameConfig.absenteeismList(), startCell + ":" + endCell);
        return sheetDateLine.map(arrayLists -> {
            String s = "10л";
            for (int i = 0; i < arrayLists.size(); i++) {
                if (!arrayLists.get(i).isEmpty()) {
                    s += "\n" + allStudents.get(i).getStudentName() + " ";
                    String s1 = arrayLists.get(i).getFirst();
                    int start = 0;
                    int end = 0;
                    for (int j = 1; j < arrayLists.get(i).size(); j++) {
                        if (!arrayLists.get(i).get(j).equals(s1)) {
                            end = j - 1;
                            s += generateTextAbsenteeismLine(start, end, s1);
                            s1 = arrayLists.get(i).get(j);
                            start = j;
                        }
                    }
                    if (end != arrayLists.get(i).size()) {
                        s += generateTextAbsenteeismLine(start, arrayLists.get(i).size() - 1, s1);
                    }
                }
            }
            return s;
        }).orElse("Не найдено");
    }

    private String generateTextAbsenteeismLine(Integer start, Integer end, String type) {
        String ret = "";
        if (!type.isEmpty()) {
            if (start.equals(end)) {
                ret += start + " урок " + typeAndValueOfAbsenteeism.get(type) + ". ";
            }
            if (!start.equals(end)) {
                ret += start + "-" + end + " уроки " + typeAndValueOfAbsenteeism.get(type) + ". ";
            }
        }
        return ret;
    }

    public String linesToString(Optional<List<List<String>>>... values) {
        List<List<String>> returnedList = new ArrayList<>();
        for (Optional<List<List<String>>> firstValue : values) {
            firstValue.ifPresent(arrayLists -> returnedList.add(arrayLists.getFirst()));
        }
        String returnedText = "";
        if (returnedList.size() > 1) {
            int lastNumberListOfValues = returnedList.size() - 1;
            List<String> lastList = returnedList.get(lastNumberListOfValues);
            int countElementsInLastListOfValues = lastList.size();
            for (int i = 0; i < countElementsInLastListOfValues; i++) {
                if (lastList.get(i) != null && !lastList.get(i).isEmpty()) {
                    for (List<String> strings : returnedList) {
                        if (i < strings.size()) {
                            returnedText += strings.get(i) + " ";
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
        List<String> receivedData = dialogAttribute.getReceivedData();
        Optional<Student> optionalStudent = studentsRepository.findByNumber(receivedData.get(0));
        if (optionalStudent.isPresent() && receivedData.size() == 4) {
            Student student = optionalStudent.get();
            int startOfAbsenteeism = Integer.parseInt(receivedData.get(1));
            int endOfAbsenteeism = Integer.parseInt(receivedData.get(2));
            String typeOfAbsenteeism = receivedData.get(3);
            List<Object> list = new ArrayList<>();
            if (endOfAbsenteeism >= startOfAbsenteeism) {
                for (int i = 0; i <= startOfAbsenteeism; i++) {
                    list.add(i, null);
                }
                for (int i = startOfAbsenteeism; i <= endOfAbsenteeism; i++) {
                    list.add(i, typeOfAbsenteeism);
                }
            }
            List<List<Object>> arrayLists = List.of(list);
            Integer columnNumber = LocalDateTime.now().getDayOfMonth() * 8 - 7 + 2 + startOfAbsenteeism;
            String startCell = studentService.getNameStartCellOfAbsenteeism(student, columnNumber);
            sheetListener.writeSheet(sheetListNameConfig.absenteeismList(), startCell, arrayLists);
            return true;
        }
        return false;
    }

    @PostConstruct
    private void setUp() {
        Optional<List<List<String>>> absenteeism = sheetListener.getSheetList(sheetListNameConfig.absenteeismList(), fieldsNameConfig.typeOfAbsenteeism());
        List<List<String>> arrayLists = absenteeism.get();
        for (List<String> arrayList : arrayLists) {
            typeAndValueOfAbsenteeism.put(arrayList.get(0), arrayList.get(1));
        }
    }

    public List<SendMessage> sendMessages(DialogAttribute dialogAttribute) {
        List<String> receivedData = dialogAttribute.getReceivedData();
        List<SendMessage> sendMessages = new ArrayList<>();

        if (receivedData.size() == 4) {
            String classParalleles = receivedData.get(0);
            String classLetters = receivedData.get(1);
            String sex = receivedData.get(2);
            String text = receivedData.get(3);
            List<User> collect = userRepository.getAllUsers().stream()
                    .filter(u -> {
                        if (classParalleles.equals("all")) {
                            return true;
                        } else {
                            return u.getClassParallel().equals(classParalleles);
                        }
                    })
                    .filter(u -> {
                        if (classLetters.equals("all")) {
                            return true;
                        } else {
                            return u.getClassLetter().equals(classLetters);
                        }
                    })
                    .filter(u -> {
                        if (sex.equals("all")) {
                            return true;
                        } else {
                            return u.getSex().equals(sex);
                        }
                    })
                    .toList();
            if (text.equals("lab_nb")) {
                for (User user : collect) {
                    if (getStudentLaboratoryNotebook(user).isEmpty()) {
                        sendMessages.add(SendMessage.builder()
                                .chatId(user.getTelegramUserId())
                                .text("Принеси тетрадь для лабораторных работ!")
                                .build());
                    }
                }
            } else if (text.equals("test_nb")) {
                for (User user : collect) {
                    if (getStudentTestNotebook(user).isEmpty()) {
                        sendMessages.add(SendMessage.builder()
                                .chatId(user.getTelegramUserId())
                                .text("Принеси тетрадь для контрольных работ!")
                                .build());
                    }
                }
            } else {
                for (User user : collect) {
                    sendMessages.add(SendMessage.builder()
                            .chatId(user.getTelegramUserId())
                            .text(text)
                            .build());
                }
            }
        }
        return sendMessages;
    }
}