package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.SheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkingAbsenteeismHandler implements Handler {

    private final SheetService sheetService;
    private final DialogAttributesService dialogAttributesService;
    private final SchoolConfig schoolConfig;
    private final StudentsRepository studentsRepository;
    private final UserRepository userRepository;

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

            Optional<User> optionalUser = userRepository.findById(chatId);
            if (update.getMessage().getText().equals("/absenteeism")) {
                optionalUser.ifPresentOrElse(user -> {
                            if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                                sendMessages.add(SendMessage.builder()
                                        .chatId(chatId)
                                        .text(CLASS_STUDENTS)
                                        .replyMarkup(getKeyboard(studentsRepository.getAllStudents()))
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
            dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                switch (dialogAttribute.getStepOfDialog()) {
                    case 0 -> {
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        studentsRepository.findByNumber(update.getCallbackQuery().getData()).ifPresentOrElse(student -> {
                                    sendMessages.add(EditMessageText.builder()
                                            .chatId(chatId)
                                            .text(student.getStudentName())
                                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                            .build());
                                    sendMessages.add(getKeyboard(update,
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
                        sendMessages.add(getKeyboard(update,
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
                        sendMessages.add(getKeyboard(update,
                                TYPE_OF_ABSENTEEISM,
                                sheetService.getTypeAndValueOfAbsenteeism()));
                    }
                    case 3 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(sheetService.getTypeAndValueOfAbsenteeism().get(update.getCallbackQuery().getData()))
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IN_PROGRESS)
                                .build());
                        if (sheetService.writeAbsenteeism(dialogAttribute)) {
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
        }
        return sendMessages;
    }

    private SendMessage getKeyboard(Update update, String text, Map<String, String> map) {
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
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private SendMessage getKeyboard(Update update, String text, List<String> callbackData) {
        return getKeyboard(update, text, callbackData, callbackData);
    }

    private SendMessage getKeyboard(Update update, String text, List<String> callbackData, List<String> labels) {
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
                .chatId(update.getCallbackQuery().getMessage().getChatId())
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
}