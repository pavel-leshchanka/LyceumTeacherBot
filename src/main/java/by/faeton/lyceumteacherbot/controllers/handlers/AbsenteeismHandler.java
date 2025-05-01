package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.AbsenteeismDTO;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateNumberOfSubject;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.model.lyceum.Journal;
import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.utils.DefaultMessages;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_STUDENTS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.END_MARK_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START_MARK_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.STUDENT_NOT_FOUND_PLEASE_REPEATE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TYPE_OF_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_NOT_COMPLETED;

@Slf4j
@Component
public class AbsenteeismHandler extends Handler {
    private final SchoolConfig schoolConfig;
    private final JournalService journalService;
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;

    public AbsenteeismHandler(DialogAttributesService dialogAttributesService, SchoolConfig schoolConfig, JournalService journalService, JournalRepository journalRepository, UserRepository userRepository, StudentsRepository studentsRepository, TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository) {
        super(dialogAttributesService);
        this.schoolConfig = schoolConfig;
        this.journalService = journalService;
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
        this.studentsRepository = studentsRepository;
        this.typeAndValueOfAbsenteeismRepository = typeAndValueOfAbsenteeismRepository;
    }

    @Override
    DialogType getType() {
        return DialogType.ABSENTEEISM;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            AbsenteeismDTO absenteeismDTO = new AbsenteeismDTO();
            userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                    if (user.getUserLevel().ordinal() >= UserLevel.TEACHER.ordinal()) {
                        Set<String> classParallels = journalRepository.getClassParallels();
                        List<Pair<String, String>> list = classParallels.stream()
                            .map(e -> {
                                absenteeismDTO.setClassParallel(e);
                                String ss = generateJsonWithPrefix(absenteeismDTO);
                                return new Pair<>(e, ss);
                            })
                            .toList();
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

        if (update.hasCallbackQuery()) {
            AbsenteeismDTO absenteeismDTO;
            absenteeismDTO = getDTOFromCallback(update, AbsenteeismDTO.class);
            if (absenteeismDTO.getClassLetter() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(absenteeismDTO.getClassParallel())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> classLetters = journalRepository.getClassLetters();
                List<Pair<String, String>> list = classLetters.stream()
                    .map(e -> {
                            absenteeismDTO.setClassLetter(e);
                            String ss = generateJsonWithPrefix(absenteeismDTO);
                            return new Pair<>(e, ss);
                        }
                    )
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                    CLASS_LETTER,
                    list
                ));
            } else if (absenteeismDTO.getStudentId() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(absenteeismDTO.getClassLetter())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Student> studentsFromClass = journalService.getStudentsFromClass(absenteeismDTO.getClassLetter(), absenteeismDTO.getClassParallel(), schoolConfig.currentAcademicYear());
                List<Pair<String, String>> list = studentsFromClass.stream()
                    .map(student -> {
                        String s = student.getUserLastName() + " " + student.getUserFirstName();
                        absenteeismDTO.setStudentId(student.getStudentId());
                        String s1 = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(s, s1);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_STUDENTS, list));
            } else if (absenteeismDTO.getStartOfAbsenteeism() == null) {
                studentsRepository.findByStudentId(absenteeismDTO.getStudentId()).ifPresentOrElse(student -> {
                        sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text(student.getUserLastName() + " " + student.getUserFirstName())
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                        List<Pair<String, String>> pairs = getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson()).stream()
                            .map(s -> {
                                absenteeismDTO.setStartOfAbsenteeism(s);
                                String s1 = generateJsonWithPrefix(absenteeismDTO);
                                return new Pair<>(s, s1);
                            })
                            .toList();
                        sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                            DefaultMessages.START_ABSENTEEISM,
                            pairs));
                    }, () -> {
                        EditMessageText.builder()
                            .chatId(chatId)
                            .text(STUDENT_NOT_FOUND_PLEASE_REPEATE)
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build();
                        dialogAttributesService.remove(chatId);
                    }
                );
            } else if (absenteeismDTO.getEndOfAbsenteeism() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(START_MARK_ABSENTEEISM + absenteeismDTO.getStartOfAbsenteeism())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> pairs = getClassesNumbers(Integer.parseInt(absenteeismDTO.getStartOfAbsenteeism()), schoolConfig.lastLesson()).stream()
                    .map(s -> {
                        absenteeismDTO.setEndOfAbsenteeism(s);
                        String s1 = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(s, s1);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                    DefaultMessages.END_ABSENTEEISM,
                    pairs));
            } else if (absenteeismDTO.getTypeOfAbsenteeism() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(END_MARK_ABSENTEEISM + absenteeismDTO.getEndOfAbsenteeism())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> allTypeAndValueOfAbsenteeism = typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism().entrySet().stream()
                    .map(e -> {
                        absenteeismDTO.setTypeOfAbsenteeism(e.getKey());
                        String s1 = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(e.getValue(), s1);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, TYPE_OF_ABSENTEEISM, allTypeAndValueOfAbsenteeism));
            } else {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(absenteeismDTO.getTypeOfAbsenteeism()))
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(WRITING_IN_PROGRESS)
                    .build());
                if (writeAbsenteeism(absenteeismDTO)) {
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(WRITING_IS_COMPLETED)
                        .build());
                } else {
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(WRITING_IS_NOT_COMPLETED)
                        .build());
                }
                String classParallel = absenteeismDTO.getClassParallel();
                String classLetter = absenteeismDTO.getClassLetter();
                Optional<Journal> byClassLetterAndClassParallelAndYear = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, schoolConfig.currentAcademicYear());
                if (byClassLetterAndClassParallelAndYear.isPresent()) {
                    Journal journal = byClassLetterAndClassParallelAndYear.get();
                    Teacher classroomTeacher = journal.getClassroomTeacher();
                    String teacherId = classroomTeacher.getTeacherId();
                    Optional<User> oUser = userRepository.findBySubjectOfEducationId(teacherId).stream().findFirst();
                    oUser.ifPresent(user -> {
                        String studentId = absenteeismDTO.getStudentId();
                        Optional<Student> optionalStudent = studentsRepository.findByStudentId(studentId);
                        if (!chatId.equals(user.getTelegramUserId())) {
                            sendMessages.add(SendMessage.builder()
                                .chatId(user.getTelegramUserId())
                                .text(classroomTeacher.getName() + " внес новый пропуск в ваш класс у ученика: " + optionalStudent.get().getUserLastName())
                                .build());
                        }
                    });
                }
            }
        }
        return sendMessages;
    }

    private boolean writeAbsenteeism(AbsenteeismDTO dialogAttribute) {
        int startOfAbsenteeism = Integer.parseInt(dialogAttribute.getStartOfAbsenteeism());
        int endOfAbsenteeism = Integer.parseInt(dialogAttribute.getEndOfAbsenteeism());

        Optional<Student> optionalStudent = studentsRepository.findByStudentId(dialogAttribute.getStudentId());
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            List<NumberDateNumberOfSubject> list = new ArrayList<>();
            for (int i = startOfAbsenteeism; i <= endOfAbsenteeism; i++) {
                list.add(new NumberDateNumberOfSubject(
                    dialogAttribute.getTypeOfAbsenteeism(), LocalDate.now(), i
                ));
            }
            journalService.writeAbsenteeism(list, student, dialogAttribute.getClassParallel(), dialogAttribute.getClassLetter(), schoolConfig.currentAcademicYear());
            return true;
        }
        return false;
    }

    private List<String> getClassesNumbers(Integer startClass, Integer endClass) {
        return IntStream.rangeClosed(startClass, endClass)
            .mapToObj(String::valueOf)
            .toList();
    }
}
