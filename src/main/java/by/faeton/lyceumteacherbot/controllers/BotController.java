package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Component
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final SheetService sheetService;
    private final UserRepository userRepository;


    private final HashMap<String, String> sendFirstMessage = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(BotController.class);

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message != null && message.getText() != null) {
            String chatId = message.getChatId().toString();
            String receivedMessage = message.getText();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            Optional<User> optionalUser = userRepository.findById(chatId);

            if (sendFirstMessage.containsKey(chatId)) {
                sendMessage.setText(update.getMessage().getText());
                for (User user : userRepository.getAllUsers()) {
                    String id = user.getId();
                    if (id != null && !id.equals("")) {
                        sendMessage.setChatId(id);
                        sendUserMessage(sendMessage);
                    }
                }
                sendFirstMessage.clear();
            } else {

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
                            sendFirstMessage.put(chatId, null);
                        } else {
                            sendMessage.setText(NO_ACCESS);
                        }
                    }
                    default -> {
                        log.debug("Arrived another message: " + receivedMessage + " from user " + chatId);
                        sendMessage.setText(arrivedAnother());
                    }

                }
                sendUserMessage(sendMessage);
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