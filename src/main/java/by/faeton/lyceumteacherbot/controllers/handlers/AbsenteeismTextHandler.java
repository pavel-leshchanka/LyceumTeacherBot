package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.AbsenteeismTextDTO;
import by.faeton.lyceumteacherbot.model.DTO.StudentWithNumberAndNumberOfTask;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ABSENTEEISM_NOT_FOUND;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.DASH;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.POINT;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASKS;

@Slf4j
@Component
public class AbsenteeismTextHandler extends Handler {

    private final JournalService journalService;
    private final UserRepository userRepository;
    private final JournalRepository journalRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final SchoolConfig schoolConfig;

    public AbsenteeismTextHandler(DialogAttributesService dialogAttributesService, JournalService journalService, UserRepository userRepository, JournalRepository journalRepository, TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository, SchoolConfig schoolConfig) {
        super(dialogAttributesService);
        this.journalService = journalService;
        this.userRepository = userRepository;
        this.journalRepository = journalRepository;
        this.typeAndValueOfAbsenteeismRepository = typeAndValueOfAbsenteeismRepository;
        this.schoolConfig = schoolConfig;
    }

    @Override
    DialogType getType() {
        return DialogType.ABSENTEEISM_TEXT;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals(DialogType.ABSENTEEISM_TEXT.getCommand()) && dialogAttributesService.get(chatId) == null) {
                userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                        if (user.getUserLevel().ordinal() >= UserLevel.TEACHER.ordinal()) {
                            AbsenteeismTextDTO absenteeismTextDTO = new AbsenteeismTextDTO();

                            Set<String> classParallels = journalRepository.getClassParallels();
                            List<Pair<String, String>> list = classParallels.stream()
                                .map(e ->{
                                    absenteeismTextDTO.setClassParallel(e);
                                    String s = generateJsonWithPrefix(absenteeismTextDTO);
                                    return new Pair<>(e, s);
                                }).toList();
                            sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                                CLASS_PARALLEL,
                                list
                            ));
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
        }

        if (update.hasCallbackQuery()) {
                AbsenteeismTextDTO dtoFromCallback = getDTOFromCallback(update, AbsenteeismTextDTO.class);
                if (dtoFromCallback.getClassLetter() == null){
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(dtoFromCallback.getClassParallel())
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .build());
                    Set<String> classLetters = journalRepository.getClassLetters();
                    List<Pair<String, String>> list = classLetters.stream()
                        .map(e ->{
                            dtoFromCallback.setClassLetter(e);
                            String s = generateJsonWithPrefix(dtoFromCallback);
                            return new Pair<>(e, s);})
                        .toList();
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        CLASS_LETTER,
                        list
                    ));
                } else {
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(dtoFromCallback.getClassLetter())
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .build());

                    String classParallel = dtoFromCallback.getClassParallel();
                    String classLetter = dtoFromCallback.getClassLetter();

                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(getTextOfAbsenteeismOnCurrentDate(LocalDate.now(), classLetter, classParallel))
                        .build());
                }
        }
        return sendMessages;
    }

    private String getTextOfAbsenteeismOnCurrentDate(LocalDate localDate, String classLetter, String classParallel) {
        List<StudentWithNumberAndNumberOfTask> studentsAbsenteeism = journalService.getStudentsAbsenteeism(localDate, classLetter, classParallel, schoolConfig.currentAcademicYear());
        Map<String, List<StudentWithNumberAndNumberOfTask>> collect = studentsAbsenteeism.stream()
            .collect(Collectors.groupingBy(StudentWithNumberAndNumberOfTask::getStudentName, Collectors.toList()));
        String s = classParallel + classLetter;
        if (!collect.isEmpty()) {
            for (List<StudentWithNumberAndNumberOfTask> studentWithNumberAndNumberOfTasks : collect.values()) {
                s += "\n" + studentWithNumberAndNumberOfTasks.get(0).getStudentName() + " ";
                studentWithNumberAndNumberOfTasks.sort(Comparator.comparing(StudentWithNumberAndNumberOfTask::getNumberOfTask));
                String s1 = studentWithNumberAndNumberOfTasks.get(0).getNumber();
                int start = studentWithNumberAndNumberOfTasks.get(0).getNumberOfTask();
                int current = start;
                int end;
                for (int j = 1; j < studentWithNumberAndNumberOfTasks.size(); j++) {
                    if (!studentWithNumberAndNumberOfTasks.get(j).getNumber().equals(s1) || !studentWithNumberAndNumberOfTasks.get(j).getNumberOfTask().equals(current + 1)) {
                        end = studentWithNumberAndNumberOfTasks.get(j - 1).getNumberOfTask();
                        s += generateTextAbsenteeismLine(start, end, s1);
                        s1 = studentWithNumberAndNumberOfTasks.get(j).getNumber();
                        start = studentWithNumberAndNumberOfTasks.get(j).getNumberOfTask();
                        current = start;
                    } else {
                        current++;
                    }
                }
                s += generateTextAbsenteeismLine(start, studentWithNumberAndNumberOfTasks.get(studentWithNumberAndNumberOfTasks.size() - 1).getNumberOfTask(), s1);
            }
        } else {
            s += "\n" + ABSENTEEISM_NOT_FOUND;
        }
        return s;
    }

    private String generateTextAbsenteeismLine(Integer start, Integer end, String type) {
        String ret = "";
        if (!type.isEmpty()) {
            if (start.equals(end)) {
                ret += start + TASK + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + POINT;
            }
            if (!start.equals(end)) {
                ret += start + DASH + end + TASKS + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + POINT;
            }
        }
        return ret;
    }
}

