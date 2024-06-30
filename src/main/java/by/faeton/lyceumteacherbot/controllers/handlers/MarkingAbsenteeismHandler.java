package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository1;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.repositories.SheetListener;
import by.faeton.lyceumteacherbot.utils.addressgenerator.StudentCellAddressGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_STUDENTS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.END_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TYPE_OF_ABSENTEEISM;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_NOT_COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkingAbsenteeismHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;
    private final SchoolConfig schoolConfig;
    private final StudentsRepository1 studentsRepository1;
    private final UserRepository userRepository;
    private final SheetListener sheetListener;
    private final SheetListNameConfig sheetListNameConfig;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final StudentCellAddressGenerator studentCellAddressGenerator;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals("/absenteeism")) {
                return true;
            }
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.ABSENTEEISM))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
            if (update.getMessage().getText().equals("/absenteeism")) {
                optionalUser.ifPresentOrElse(user -> {
                            if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                                sendMessages.add(SendMessage.builder()
                                        .chatId(chatId)
                                        .text(CLASS_STUDENTS)
                                        .replyMarkup(getKeyboard(studentsRepository1.getAllStudentsForClass(user.getClassParallel() + user.getClassParallel())))
                                        .build());
                                dialogAttributesService.createDialog(DialogTypeStarted.ABSENTEEISM, chatId);
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
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            userRepository.findByTelegramId(chatId).ifPresent(user -> {
                dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                    switch (dialogAttribute.getStepOfDialog()) {
                        case 0 -> {
                            dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                            studentsRepository1.findByNumber(update.getCallbackQuery().getData(), user.getClassParallel() + user.getClassLetter()).ifPresentOrElse(student -> {
                                        sendMessages.add(EditMessageText.builder()
                                                .chatId(chatId)
                                                .text(student.getStudentName())
                                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                                .build());
                                        sendMessages.add(getKeyboard(chatId,
                                                START_ABSENTEEISM,
                                                getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson())));
                                    }, () -> {
                                        EditMessageText.builder()
                                                .chatId(chatId)
                                                .text("Ученик не найден, повторите попытку.")
                                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                                .build();
                                        dialogAttributesService.deleteByTelegramId(chatId);
                                    }
                            );
                        }
                        case 1 -> {
                            dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                            sendMessages.add(EditMessageText.builder()
                                    .chatId(chatId)
                                    .text("Начало пропуска: " + update.getCallbackQuery().getData())
                                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                    .build());
                            sendMessages.add(getKeyboard(chatId,
                                    END_ABSENTEEISM,
                                    getClassesNumbers(Integer.parseInt(update.getCallbackQuery().getData()), schoolConfig.lastLesson())));
                        }
                        case 2 -> {
                            dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                            sendMessages.add(EditMessageText.builder()
                                    .chatId(chatId)
                                    .text("Конец пропуска: " + update.getCallbackQuery().getData())
                                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                    .build());
                            sendMessages.add(getKeyboard(chatId,
                                    TYPE_OF_ABSENTEEISM,
                                    typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism()));
                        }
                        case 3 -> {
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
                            if (writeAbsenteeism(dialogAttribute, user.getClassParallel() + user.getClassLetter())) {
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
                            dialogAttributesService.deleteByTelegramId(chatId);
                        }
                    }
                });
            });
        }
        return sendMessages;
    }

    private SendMessage getKeyboard(Long chatId, String text, Map<String, String> map) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        map.forEach((key, value) -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(value)
                    .callbackData(key)
                    .build());
            rowsInline.add(row);
        });
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Отмена")
                .callbackData("Cancel")
                .build());
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
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
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (int i = 0; i < callbackData.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(labels.get(i)))
                    .callbackData(String.valueOf(callbackData.get(i)))
                    .build());
            rowsInline.add(row);
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Отмена")
                .callbackData("Cancel")
                .build());
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private InlineKeyboardMarkup getKeyboard(List<Student> students) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        students.forEach(student -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(student.getStudentName())
                    .callbackData(student.getStudentNumber())
                    .build());
            rowsInline.add(row);
        });
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Отмена")
                .callbackData("Cancel")
                .build());
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
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

    public boolean writeAbsenteeism(DialogAttribute dialogAttribute, String classParallelAndLetter) {
        List<String> receivedData = dialogAttribute.getReceivedData();
        Optional<Student> optionalStudent = studentsRepository1.findByNumber(receivedData.get(0), classParallelAndLetter);
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
            String startCell = studentCellAddressGenerator.getNameOfStartCellOfAbsenteeism(student, columnNumber);
            sheetListener.writeSheet(sheetListNameConfig.absenteeismList() + classParallelAndLetter, startCell, arrayLists);
            return true;
        }
        return false;
    }
}