package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository1;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.repositories.SheetListener;
import by.faeton.lyceumteacherbot.utils.addressgenerator.CellAddressGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;

@Slf4j
@Component
@RequiredArgsConstructor
public class AbsenteeismTextHandler implements Handler {
    private final UserRepository userRepository;
    private final StudentsRepository1 studentsRepository1;
    private final SheetListener sheetListener;
    private final SheetListNameConfig sheetListNameConfig;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final CellAddressGenerator cellAddressGenerator;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals("/absenteeism_text")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        switch (message.getText()) {
            case "/absenteeism_text" -> userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                        if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(getTextOfAbsenteeism(user.getClassParallel() + user.getClassLetter()))
                                    .build());
                        } else {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(NO_ACCESS)
                                    .build());
                        }
                    },
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));

        }
        return sendMessages;
    }

    public String getTextOfAbsenteeism(String classParallelAndLetter) {
        List<Student> allStudents = studentsRepository1.getAllStudentsForClass(classParallelAndLetter);
        int columnNumber = LocalDateTime.now().getDayOfMonth() * 8 - 7 + 2;
        Student student = new Student();
        student.setStudentNumber(String.valueOf(1));
        String startCell = cellAddressGenerator.getNameOfStartCellOfAbsenteeism(student, columnNumber);
        student.setStudentNumber(String.valueOf(allStudents.size()));
        String endCell = cellAddressGenerator.getNameOfStartCellOfAbsenteeism(student, columnNumber + 7);
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetList(sheetListNameConfig.absenteeismList() + classParallelAndLetter, startCell + ":" + endCell);
        return sheetDateLine.map(arrayLists -> {
            String s = classParallelAndLetter;
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
                ret += start + " урок " + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + ". ";
            }
            if (!start.equals(end)) {
                ret += start + "-" + end + " уроки " + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + ". ";
            }
        }
        return ret;
    }
}
