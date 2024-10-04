package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateNumberOfSubject;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;
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
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.ABSENTEEISM_COMMAND;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkingAbsenteeismHandler implements Handler {


    private final DialogAttributesService dialogAttributesService;
    private final SchoolConfig schoolConfig;
    private final JournalService journalService;
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals(ABSENTEEISM_COMMAND)) {
                return true;
            }
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.REGISTER_ABSENTEEISM))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                        if (user.getUserLevel().ordinal() >= UserLevel.TEACHER.ordinal()) {
                            Set<String> classParallels = journalRepository.getClassParallels();
                            sendMessages.add(getKeyboard(chatId,
                                    CLASS_PARALLEL,
                                    classParallels
                            ));
                            dialogAttributesService.createDialog(DialogTypeStarted.REGISTER_ABSENTEEISM, chatId);
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
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            userRepository.findByTelegramId(chatId).flatMap(user -> dialogAttributesService.find(chatId)).ifPresent(dialogAttribute -> {
                switch (dialogAttribute.getStepOfDialog()) {
                    case 0 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        Set<String> classLetters = journalRepository.getClassLetters();
                        sendMessages.add(getKeyboard(chatId,
                                CLASS_LETTER,
                                classLetters
                        ));
                    }
                    case 1 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        String classParallel = dialogAttribute.getReceivedData().get(0);
                        String classLetter = dialogAttribute.getReceivedData().get(1);
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(CLASS_STUDENTS)
                                .replyMarkup(getKeyboard(journalService.getStudentsFromClass(classLetter, classParallel, schoolConfig.currentAcademicYear())))
                                .build());
                    }

                    case 2 -> {
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        studentsRepository.findByStudentId(update.getCallbackQuery().getData()).ifPresentOrElse(student -> {
                                    sendMessages.add(EditMessageText.builder()
                                            .chatId(chatId)
                                            .text(student.getUserLastName() + " " + student.getUserFirstName())
                                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                            .build());
                                    sendMessages.add(getKeyboard(chatId,
                                            DefaultMessages.START_ABSENTEEISM,
                                            getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson())));
                                }, () -> {
                                    EditMessageText.builder()
                                            .chatId(chatId)
                                            .text(STUDENT_NOT_FOUND_PLEASE_REPEATE)
                                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                            .build();
                                    dialogAttributesService.deleteByTelegramId(chatId);
                                }
                        );
                    }
                    case 3 -> {
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(START_MARK_ABSENTEEISM + update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        sendMessages.add(getKeyboard(chatId,
                                DefaultMessages.END_ABSENTEEISM,
                                getClassesNumbers(Integer.parseInt(update.getCallbackQuery().getData()), schoolConfig.lastLesson())));
                    }
                    case 4 -> {
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(END_MARK_ABSENTEEISM + update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        sendMessages.add(getKeyboard(chatId,
                                TYPE_OF_ABSENTEEISM,
                                typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism()));
                    }
                    case 5 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(update.getCallbackQuery().getData()))
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IN_PROGRESS)
                                .build());
                        if (writeAbsenteeism(dialogAttribute)) {
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

                        List<String> receivedData = dialogAttribute.getReceivedData();
                        String classParallel = receivedData.get(0);
                        String classLetter = receivedData.get(1);
                        Optional<Journal> byClassLetterAndClassParallelAndYear = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, schoolConfig.currentAcademicYear());
                        if (byClassLetterAndClassParallelAndYear.isPresent()) {
                            Journal journal = byClassLetterAndClassParallelAndYear.get();
                            Teacher classroomTeacher = journal.getClassroomTeacher();
                            String teacherId = classroomTeacher.getTeacherId();
                            User user = userRepository.findBySubjectOfEducationId(teacherId).get(0);
                            String studentId = receivedData.get(2);
                            Optional<Student> optionalStudent = studentsRepository.findByStudentId(studentId);
                            if (chatId != user.getTelegramUserId()) {
                                sendMessages.add(SendMessage.builder()
                                        .chatId(user.getTelegramUserId())
                                        .text(classroomTeacher.getName() + " внес новый пропуск в ваш класс у ученика: " + optionalStudent.get().getUserLastName())
                                        .build());
                            }
                        }

                        dialogAttributesService.deleteByTelegramId(chatId);
                    }
                }
            });
        }
        return sendMessages;
    }

    private boolean writeAbsenteeism(DialogAttribute dialogAttribute) {
        List<String> receivedData = dialogAttribute.getReceivedData();
        String classParallel = receivedData.get(0);
        String classLetter = receivedData.get(1);
        String studentId = receivedData.get(2);
        int startOfAbsenteeism = Integer.parseInt(receivedData.get(3));
        int endOfAbsenteeism = Integer.parseInt(receivedData.get(4));
        String typeOfAbsenteeism = receivedData.get(5);

        Optional<Student> optionalStudent = studentsRepository.findByStudentId(studentId);
        if (optionalStudent.isPresent() && receivedData.size() == 6) {
            Student student = optionalStudent.get();
            List<NumberDateNumberOfSubject> list = new ArrayList<>();
            for (int i = startOfAbsenteeism; i <= endOfAbsenteeism; i++) {
                list.add(new NumberDateNumberOfSubject(
                        typeOfAbsenteeism, LocalDate.now(), i
                ));
            }
            journalService.writeAbsenteeism(list, student, classParallel, classLetter, schoolConfig.currentAcademicYear());
            return true;
        }
        return false;
    }

    private SendMessage getKeyboard(Long chatId, String text, Map<String, String> map) {
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        map.forEach((key, value) -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(value)
                    .callbackData(key)
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        });
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboard(rowsInline)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private SendMessage getKeyboard(Long chatId, String text, List<String> callbackData) {
        return getKeyboard(chatId, text, callbackData, callbackData);
    }

    private SendMessage getKeyboard(Long chatId, String text, List<String> callbackData, List<String> labels) {
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        for (int i = 0; i < callbackData.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(labels.get(i)))
                    .callbackData(String.valueOf(callbackData.get(i)))
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboard(rowsInline)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private InlineKeyboardMarkup getKeyboard(List<Student> students) {
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        students.forEach(student -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(student.getUserLastName() + " " + student.getUserFirstName())
                    .callbackData(student.getStudentId())
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        });
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
        return InlineKeyboardMarkup.builder()
                .keyboard(rowsInline)
                .build();
    }

    private List<String> getClassesNumbers(Integer startClass, Integer endClass) {
        List<String> numbers = new ArrayList<>();
        if (endClass >= startClass) {
            for (int i = startClass; i <= endClass; i++) {
                numbers.add(String.valueOf(i));
            }
        }
        return numbers;
    }

    private SendMessage getKeyboard(Long chatId, String text, Set<String> callbackData) {

        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        for (String s : callbackData) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(s)
                    .callbackData(s)
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboard(rowsInline)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }
}