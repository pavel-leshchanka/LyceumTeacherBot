package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.*;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.SheetService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Component
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SheetService sheetService;
    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;


    private static final Logger log = LoggerFactory.getLogger(BotController.class);

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage = new SendMessage();

        if (update.hasMessage()) {
            updateHasMessage(update, sendMessage);
        }
        if (update.hasCallbackQuery()) {
            updateHasCallbackQuery(update, sendMessage);
        }


    }

    private void updateHasCallbackQuery(Update update, SendMessage sendMessage) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);


        Optional<DialogAttribute> byId = dialogAttributesService.find(chatId);

        if (byId.isPresent()) {

            if (byId.get().getDialogTypeStarted().equals(DialogTypeStarted.ABSENTEEISM)) {
                if (byId.get().getStepOfDialog() == 0) {
                    dialogAttributesService.nextStep(byId.get(), update.getCallbackQuery().getData());
                    InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup(List.of(0, 1, 2, 3, 4, 5, 6, 7));
                    sendMessage.setReplyMarkup(markupInline);
                    sendMessage.setText("Начало пропуска");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                } else if (byId.get().getStepOfDialog() == 1) {
                    dialogAttributesService.nextStep(byId.get(), update.getCallbackQuery().getData());
                    InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup(List.of(0, 1, 2, 3, 4, 5, 6, 7));
                    sendMessage.setReplyMarkup(markupInline);
                    sendMessage.setText("Конец пропуска");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                } else if (byId.get().getStepOfDialog() == 2) {
                    dialogAttributesService.nextStep(byId.get(), update.getCallbackQuery().getData());
                    InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup(Arrays.asList(sheetService.getTypeAndValueOfAbsenteeism().keySet().toArray()), Arrays.asList(sheetService.getTypeAndValueOfAbsenteeism().values().toArray()));
                    sendMessage.setReplyMarkup(markupInline);
                    sendMessage.setText("Тип пропуска");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                } else if (byId.get().getStepOfDialog() == 3) {
                    dialogAttributesService.nextStep(byId.get(), update.getCallbackQuery().getData());
                    sendMessage.setText("Выполняется запись");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                    boolean isWritten = sheetService.writeAbsenteeism(byId.get());

                    if (isWritten) {
                        sendMessage.setText("Запись выполнена");
                        sendUserMessage(sendMessage);
                    } else {
                        sendMessage.setText("Запись не выполнена. Попробуйте еще раз");
                        sendUserMessage(sendMessage);
                    }
                    dialogAttributesService.finalStep(chatId);

                }
            }


        }

    }


    private void updateHasMessage(Update update, SendMessage sendMessage) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        sendMessage.setChatId(chatId);
        Optional<DialogAttribute> byId = dialogAttributesService.find(Long.valueOf(chatId));
        Optional<User> optionalUser = userRepository.findById(chatId);

        if (byId.isEmpty() && message.hasText()) {
            String receivedMessage = message.getText();

            switch (receivedMessage) {
                case "/start" -> sendMessage.setText(arrivedStart());
                case "/marks" -> {
                    if (optionalUser.isPresent()) {
                        sendMessage.setText(arrivedMarks(optionalUser.get()));
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/quarter" -> {
                    if (optionalUser.isPresent()) {
                        sendMessage.setText(arrivedQuarterMarks(optionalUser.get()));
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/laboratory_notebook" -> {
                    if (optionalUser.isPresent()) {
                        sendMessage.setText(arrivedLaboratoryNotebook(optionalUser.get()));
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/absenteeism_text" -> {
                    if (optionalUser.isPresent()) {
                        if (optionalUser.get().getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessage.setText(sheetService.getTextOfAbsenteeism());
                        } else {
                            sendMessage.setText(NO_ACCESS);
                        }
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/test_notebook" -> {
                    if (optionalUser.isPresent()) {
                        sendMessage.setText(arrivedTestNotebook(optionalUser.get()));
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/help" -> sendMessage.setText(arrivedHelp());
                case "/send_message" -> {
                    if (optionalUser.isPresent()) {
                        if (optionalUser.get().getUserLevel().equals(UserLevel.ADMIN)) {
                            sendMessage.setText(WHAT_SENDING);
                            dialogAttributesService.createDialog(DialogTypeStarted.SEND_MESSAGE, Long.valueOf(chatId));
                        } else {
                            sendMessage.setText(NO_ACCESS);
                        }
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/absenteeism" -> {
                    if (optionalUser.isPresent()) {
                        if (optionalUser.get().getUserLevel().equals(UserLevel.ADMIN)) {
                            List<Student> allStudents = studentsRepository.getAllStudents();
                            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                            for (Student student : allStudents) {
                                List<InlineKeyboardButton> row = new ArrayList<>();
                                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                                inlineKeyboardButton.setText(student.getStudentName());
                                inlineKeyboardButton.setCallbackData(student.getStudentNumber());
                                row.add(inlineKeyboardButton);
                                rowsInline.add(row);
                            }
                            markupInline.setKeyboard(rowsInline);
                            sendMessage.setReplyMarkup(markupInline);
                            sendMessage.setText("Список класса");

                            dialogAttributesService.createDialog(DialogTypeStarted.ABSENTEEISM, Long.valueOf(chatId));
                        } else {
                            sendMessage.setText(NO_ACCESS);
                        }
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                default -> {
                    log.info("Arrived another message: " + receivedMessage + " from user " + chatId);
                    sendMessage.setText(arrivedAnother());
                }

            }
            sendUserMessage(sendMessage);

        }

        if (byId.isPresent()) {
            if (byId.get().getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE)) {
                sendMessage.setText(update.getMessage().getText());
                for (User user : userRepository.getAllUsers()) {
                    String id = user.getTelegramUserId();
                    if (id != null && !id.equals("")) {
                        sendMessage.setChatId(id);
                        sendUserMessage(sendMessage);
                    }
                }
                dialogAttributesService.finalStep(Long.valueOf(chatId));
            }
        }
    }


    private String arrivedStart() {
        return START;
    }

    private String arrivedMarks(User user) {
        String sheetText = sheetService.getStudentMarks(user);
        if (sheetText.equals("")) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedQuarterMarks(User user) {
        String sheetText = sheetService.getStudentQuarterMarks(user);
        if (sheetText.equals("")) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedLaboratoryNotebook(User user) {
        String sheetText = sheetService.getStudentLaboratoryNotebook(user);
        if (sheetText.equals("")) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;
    }

    private String arrivedTestNotebook(User userId) {
        String sheetText = sheetService.getStudentTestNotebook(userId);
        if (sheetText.equals("")) {
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

    public String getBotUsername() {
        return botConfig.botName();
    }

    public String getBotToken() {
        return botConfig.botToken();
    }

    public void sendUserMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
            log.info("User " + sendMessage.getChatId() + " message arrived.");
        } catch (TelegramApiException e) {
            log.warn("User " + sendMessage.getChatId() + " message not arrived.");
        }
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup(List callbackData) {
        return getInlineKeyboardMarkup(callbackData, callbackData);
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup(List callbackData, List labels) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (int i = 0; i < callbackData.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(String.valueOf(labels.get(i)));
            inlineKeyboardButton.setCallbackData(String.valueOf(callbackData.get(i)));
            row.add(inlineKeyboardButton);
            rowsInline.add(row);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}