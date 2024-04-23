package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.*;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.SheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TextMessageHandler implements MessageHandler {

    private final SheetService sheetService;
    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Optional<DialogAttribute> byId = dialogAttributesService.find(update.getMessage().getChatId());
            return update.getMessage().hasText() && byId.isEmpty();
        }
        return false;
    }

    @Override
    public List<SendMessage> execute(Update update) {
        List<SendMessage> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Optional<User> optionalUser = userRepository.findById(chatId);
        switch (message.getText()) {
            case "/start" -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedStart())
                    .build());
            case "/marks" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedMarks(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/quarter" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedQuarterMarks(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/laboratory_notebook" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedLaboratoryNotebook(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/absenteeism_text" -> optionalUser.ifPresentOrElse(user -> {
                        if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(sheetService.getTextOfAbsenteeism())
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
            case "/test_notebook" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedTestNotebook(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/help" -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedHelp())
                    .build());
            case "/send_message" -> optionalUser.ifPresentOrElse(user -> {
                        if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(WHAT_SENDING)
                                    .build());
                            dialogAttributesService.createDialog(DialogTypeStarted.SEND_MESSAGE, Long.valueOf(chatId));
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

            case "/absenteeism" -> optionalUser.ifPresentOrElse(user -> {
                        if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(CLASS_STUDENTS)
                                    .replyMarkup(getInlineKeyboardMarkup(studentsRepository.getAllStudents()))
                                    .build());
                            dialogAttributesService.createDialog(DialogTypeStarted.ABSENTEEISM, Long.valueOf(chatId));
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
            default -> {
                log.info("Arrived another message: {} from user {}", message.getText(), chatId);
                sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(arrivedAnother())
                        .build());
            }
        }
        return sendMessages;
    }

    private String arrivedStart() {
        return START;
    }

    private String arrivedMarks(User user) {
        String sheetText = sheetService.getStudentMarks(user);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedQuarterMarks(User user) {
        String sheetText = sheetService.getStudentQuarterMarks(user);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedLaboratoryNotebook(User user) {
        String sheetText = sheetService.getStudentLaboratoryNotebook(user);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;
    }

    private String arrivedTestNotebook(User userId) {
        String sheetText = sheetService.getStudentTestNotebook(userId);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;
    }

    private String arrivedHelp() {
        return HELP;
    }

    private String arrivedAnother() {
        return ANOTHER_MESSAGES;
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup(List<Student> students) {
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
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}