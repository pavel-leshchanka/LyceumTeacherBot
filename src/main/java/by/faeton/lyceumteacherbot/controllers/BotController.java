package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogStarted;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.DialogAttributeRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Component
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SheetService sheetService;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;
    private final DialogAttributeRepository dialogAttributeRepository;


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


        Optional<DialogAttribute> byId = dialogAttributeRepository.findById(chatId);

        if (byId.isPresent()) {

            if (byId.get().getDialogStarted().equals(DialogStarted.ABSENTEEISM)) {
                if (byId.get().getStepOfDialog() == 0) {
                    ArrayList<String> receivedData = byId.get().getReceivedData();
                    receivedData.add(update.getCallbackQuery().getData());
                    byId.get().setReceivedData(receivedData);
                    byId.get().setStepOfDialog(byId.get().getStepOfDialog() + 1);
                    dialogAttributeRepository.save(byId.get());

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                        inlineKeyboardButton.setText(String.valueOf(i));
                        inlineKeyboardButton.setCallbackData(update.getCallbackQuery().getData() + i);
                        row.add(inlineKeyboardButton);
                        rowsInline.add(row);
                    }
                    markupInline.setKeyboard(rowsInline);
                    sendMessage.setReplyMarkup(markupInline);
                    sendMessage.setText("Начало пропуска");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                } else if (byId.get().getStepOfDialog() == 1) {
                    ArrayList<String> receivedData = byId.get().getReceivedData();
                    receivedData.add(update.getCallbackQuery().getData());
                    byId.get().setReceivedData(receivedData);
                    byId.get().setStepOfDialog(byId.get().getStepOfDialog() + 1);
                    dialogAttributeRepository.save(byId.get());

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    for (int i = 0; i < 8; i++) {
                        List<InlineKeyboardButton> row = new ArrayList<>();
                        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                        inlineKeyboardButton.setText(String.valueOf(i));
                        inlineKeyboardButton.setCallbackData(update.getCallbackQuery().getData() + i);
                        row.add(inlineKeyboardButton);
                        rowsInline.add(row);
                    }
                    markupInline.setKeyboard(rowsInline);
                    sendMessage.setReplyMarkup(markupInline);
                    sendMessage.setText("Конец пропуска");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                } else if (byId.get().getStepOfDialog() == 2) {
                    ArrayList<String> receivedData = byId.get().getReceivedData();
                    receivedData.add(update.getCallbackQuery().getData());
                    byId.get().setReceivedData(receivedData);
                    byId.get().setStepOfDialog(byId.get().getStepOfDialog() + 1);
                    dialogAttributeRepository.save(byId.get());

                    sendMessage.setText("Выполняется запись");
                    sendMessage.setChatId(chatId);
                    sendUserMessage(sendMessage);
                    boolean b = sheetService.writeAbsenteeism(byId.get());

                    if (b){
                        sendMessage.setText("Запись выполнена");
                        sendUserMessage(sendMessage);
                    } else {
                        sendMessage.setText("Запись не выполнена. Попробуйте еще раз");
                        sendUserMessage(sendMessage);
                    }
                    dialogAttributeRepository.deleteById(chatId);

                }
            }


        }

    }

    private void updateHasMessage(Update update, SendMessage sendMessage) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        sendMessage.setChatId(chatId);
        Optional<DialogAttribute> byId = dialogAttributeRepository.findById(Long.valueOf(chatId));
        if (byId.isEmpty() && message.hasText()) {
            String receivedMessage = message.getText();
            Optional<User> optionalUser = userRepository.findById(chatId);

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
                case "/test_notebook" -> {
                    if (optionalUser.isPresent()) {
                        sendMessage.setText(arrivedTestNotebook(optionalUser.get()));
                    } else {
                        sendMessage.setText(NOT_AUTHORIZER);
                    }
                }
                case "/help" -> sendMessage.setText(arrivedHelp());
                case "/send_message" -> {
                    if (chatId.equals(botConfig.getAdminChatId())) {
                        sendMessage.setText(WHAT_SENDING);

                        DialogAttribute dialogAttribute = new DialogAttribute();
                        dialogAttribute.setId(Long.valueOf(chatId));
                        dialogAttribute.setDialogStarted(DialogStarted.SEND_MESSAGE);
                        dialogAttributeRepository.save(dialogAttribute);

                    } else {
                        sendMessage.setText(NO_ACCESS);
                    }
                }
                case "/absenteeism" -> {
                    if (chatId.equals(botConfig.getAdminChatId())) {
                        List<Student> allStudents = studentsRepository.getAllStudents();
                        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                        for (Student student : allStudents) {
                            List<InlineKeyboardButton> row = new ArrayList<>();
                            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                            inlineKeyboardButton.setText(student.getName());
                            inlineKeyboardButton.setCallbackData(student.getNumber());
                            row.add(inlineKeyboardButton);
                            rowsInline.add(row);
                        }
                        markupInline.setKeyboard(rowsInline);
                        sendMessage.setReplyMarkup(markupInline);
                        sendMessage.setText("Список класса");

                        DialogAttribute dialogAttribute = new DialogAttribute();
                        dialogAttribute.setId(Long.valueOf(chatId));
                        dialogAttribute.setDialogStarted(DialogStarted.ABSENTEEISM);
                        dialogAttribute.setStepOfDialog(0);
                        dialogAttribute.setReceivedData(new ArrayList<>());
                        dialogAttributeRepository.save(dialogAttribute);

                    } else {
                        sendMessage.setText(NO_ACCESS);
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
            if (byId.get().getDialogStarted().equals(DialogStarted.SEND_MESSAGE)) {
                sendMessage.setText(update.getMessage().getText());
                for (User user : userRepository.getAllUsers()) {
                    String id = user.getId();
                    if (id != null && !id.equals("")) {
                        sendMessage.setChatId(id);
                        sendUserMessage(sendMessage);
                    }
                }
                dialogAttributeRepository.deleteAllById(Long.valueOf(chatId));
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
        return botConfig.getBotUsername();
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }

    public void sendUserMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
            log.info("User " + sendMessage.getChatId() + " message arrived.");
        } catch (TelegramApiException e) {
            log.warn("User " + sendMessage.getChatId() + " message not arrived.");
        }
    }
}