package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.AbsenteeismDTO;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.Teacher;
import by.faeton.lyceumteacherbot.security.TelegramUser;
import by.faeton.lyceumteacherbot.services.AbsenteeismService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.services.StudentService;
import by.faeton.lyceumteacherbot.services.TelegramUserService;
import by.faeton.lyceumteacherbot.utils.DefaultMessages;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_STUDENTS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CREATED_NEW_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.END_MARK_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START_MARK_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TYPE_OF_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_NOT_COMPLETED;

@Slf4j
@Component
public class AbsenteeismHandler extends Handler {
    private final AbsenteeismService absenteeismService;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;
    private final StudentService studentService;
    private final TelegramUserService userService;

    public AbsenteeismHandler(
        AbsenteeismService absenteeismService,
        DialogAttributesService dialogAttributesService,
        JournalService journalService,
        SchoolConfig schoolConfig,
        StudentService studentService,
        TelegramUserService userService
    ) {
        super(dialogAttributesService);
        this.absenteeismService = absenteeismService;
        this.journalService = journalService;
        this.schoolConfig = schoolConfig;
        this.studentService = studentService;
        this.userService = userService;
    }

    @Override
    DialogType getType() {
        return DialogType.ABSENTEEISM;
    }

    @Override
    @PreAuthorize("hasAuthority('TEACHER')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            AbsenteeismDTO absenteeismDTO = new AbsenteeismDTO();
            Set<String> classParallels = journalService.getClassParallels();
            List<Pair<String, String>> list = classParallels.stream()
                .map(parallel -> {
                    absenteeismDTO.setClassParallel(parallel);
                    String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                    return new Pair<>(parallel, jsonWithPrefix);
                })
                .toList();
            sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_PARALLEL, list, 4));
        }

        if (update.hasCallbackQuery()) {
            AbsenteeismDTO absenteeismDTO = getDTOFromCallback(update, AbsenteeismDTO.class);
            if (absenteeismDTO.getClassLetter() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(absenteeismDTO.getClassParallel())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> classLetters = journalService.getClassLetters();
                List<Pair<String, String>> list = classLetters.stream()
                    .map(letter -> {
                            absenteeismDTO.setClassLetter(letter);
                            String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                            return new Pair<>(letter, jsonWithPrefix);
                        }
                    )
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_LETTER, list, 4));
            } else if (absenteeismDTO.getStudentId() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(absenteeismDTO.getClassLetter())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Student> studentsFromClass = journalService.getStudentsFromClass(absenteeismDTO.getClassLetter(), absenteeismDTO.getClassParallel(), schoolConfig.currentAcademicYear());
                List<Pair<String, String>> pairList = studentsFromClass.stream()
                    .map(student -> {
                        String studentName = "%s %s".formatted(student.getUserLastName(), student.getUserFirstName());
                        absenteeismDTO.setStudentId(student.getStudentId());
                        String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(studentName, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_STUDENTS, pairList, 2));
            } else if (absenteeismDTO.getStartOfAbsenteeism() == null) {
                Student student = studentService.findByStudentId(absenteeismDTO.getStudentId());
                String studentName = "%s %s".formatted(student.getUserLastName(), student.getUserFirstName());
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(studentName)
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> pairList = getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson()).stream()
                    .map(number -> {
                        absenteeismDTO.setStartOfAbsenteeism(number);
                        String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(number, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, DefaultMessages.START_ABSENTEEISM, pairList, 4));
            } else if (absenteeismDTO.getEndOfAbsenteeism() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(START_MARK_ABSENTEEISM + absenteeismDTO.getStartOfAbsenteeism())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> pairList = getClassesNumbers(Integer.parseInt(absenteeismDTO.getStartOfAbsenteeism()), schoolConfig.lastLesson()).stream()
                    .map(number -> {
                        absenteeismDTO.setEndOfAbsenteeism(number);
                        String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(number, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, DefaultMessages.END_ABSENTEEISM, pairList, 4));
            } else if (absenteeismDTO.getTypeOfAbsenteeism() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(END_MARK_ABSENTEEISM + absenteeismDTO.getEndOfAbsenteeism())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> allTypeAndValueOfAbsenteeism = journalService.getAllTypeAndValueOfAbsenteeism().entrySet().stream()
                    .map(typeAbsenteeism -> {
                        absenteeismDTO.setTypeOfAbsenteeism(typeAbsenteeism.getKey());
                        String jsonWithPrefix = generateJsonWithPrefix(absenteeismDTO);
                        return new Pair<>(typeAbsenteeism.getValue(), jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, TYPE_OF_ABSENTEEISM, allTypeAndValueOfAbsenteeism, 2));
            } else {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(journalService.getValueOfAbsenteeism(absenteeismDTO.getTypeOfAbsenteeism()))
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(WRITING_IN_PROGRESS)
                    .build());
                if (absenteeismService.writeAbsenteeism(absenteeismDTO)) {
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(WRITING_IS_COMPLETED)
                        .build());
                    notifyClassTeacher(absenteeismDTO, chatId, sendMessages);
                } else {
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(WRITING_IS_NOT_COMPLETED)
                        .build());
                }
            }
        }
        return sendMessages;
    }

    private void notifyClassTeacher(AbsenteeismDTO absenteeismDTO, Long chatId, List<BotApiMethod> sendMessages) {
        String classParallel = absenteeismDTO.getClassParallel();
        String classLetter = absenteeismDTO.getClassLetter();
        Teacher classroomTeacher = journalService.findClassTeacher(classLetter, classParallel, schoolConfig.currentAcademicYear());
        String teacherId = classroomTeacher.getTeacherId();
        List<TelegramUser> oUser = userService.findBySubjectOfEducationId(teacherId);
        String studentId = absenteeismDTO.getStudentId();
        Student student = studentService.findByStudentId(studentId);
        oUser.forEach(user -> {
            if (!chatId.equals(user.getTelegramUserId())) {
                sendMessages.add(SendMessage.builder()
                    .chatId(user.getTelegramUserId())
                    .text(CREATED_NEW_ABSENTEEISM.formatted(classroomTeacher.getName(), student.getUserLastName()))
                    .build());
            }
        });
    }

    private List<String> getClassesNumbers(Integer startClass, Integer endClass) {
        return IntStream.rangeClosed(startClass, endClass)
            .mapToObj(String::valueOf)
            .toList();
    }
}
