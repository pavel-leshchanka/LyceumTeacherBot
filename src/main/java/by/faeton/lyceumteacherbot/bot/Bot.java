package by.faeton.lyceumteacherbot.bot;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.SheetListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    @Autowired
    private final BotConfig botConfig;
    @Autowired
    private final SheetListener sheetListener;
    @Autowired
    private final UserRepository userRepository;

    private static final String START = "Привет! Для начала работы выполни одну из возможных команд";
    private static final String HELP = "/start - Начало работы\n" +
            "/marks - Получить оценки\n" +
            "/laboratory_notebook - Проверить наличие тетради для лабораторных работ\n" +
            "/test_notebook - Проверить наличие тетради для контрольных работ\n" +
            "/help - Получить помощь";
    private static final String AVAILABLE = "В наличии";
    private static final String NOT_AVAILABLE = "Нет в наличии";
    private static final String NOT_AUTHORIZER = "Вы не авторизированы";

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String receivedMessage = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Optional<User> optionalUser = userRepository.get(chatId);
        switch (receivedMessage) {
            case "/start":
                sendMessage.setText(arrivedStart());
                break;
            case "/marks":
                if (optionalUser.isPresent()) {
                    sendMessage.setText(arrivedMarks(optionalUser.get()));
                } else {
                    sendMessage.setText(NOT_AUTHORIZER);
                }
                break;
            case "/laboratory_notebook":
                if (optionalUser.isPresent()) {
                    sendMessage.setText(arrivedLaboratoryNotebook(optionalUser.get()));
                } else {
                    sendMessage.setText(NOT_AUTHORIZER);
                }
                break;
            case "/test_notebook":
                if (optionalUser.isPresent()) {
                    sendMessage.setText(arrivedTestNotebook(optionalUser.get()));
                } else {
                    sendMessage.setText(NOT_AUTHORIZER);
                }
                break;
            case "/help":
                sendMessage.setText(arrivedHelp());
                break;
            default:
                sendMessage.setText(arrivedStart());
        }


        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String arrivedStart() {
        return START;
    }

    @SneakyThrows
    private String arrivedMarks(User user) {
        String sheetText = sheetListener.getStudentMarks(user);
        String returnedText = getLineToString(sheetText);
        if (returnedText.equals("")) {
            return NOT_AVAILABLE;
        }
        return returnedText;
    }


    @SneakyThrows
    private String arrivedLaboratoryNotebook(User user) {
        String sheetText = sheetListener.getStudentLaboratoryNotebook(user);
        String returnedText = getLineToString(sheetText);
        if (returnedText.equals("")) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;

    }

    @SneakyThrows
    private String arrivedTestNotebook(User userId) {
        String sheetText = sheetListener.getStudentTestNotebook(userId);
        String returnedText = getLineToString(sheetText);
        if (returnedText.equals("")) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;

    }

    private String arrivedHelp() {
        return HELP;
    }

    private static String getLineToString(String sheetText) throws JsonProcessingException {
        HashMap<String, Object> result = new ObjectMapper().readValue(sheetText, HashMap.class);
        ArrayList<Object> values = (ArrayList<Object>) result.get("values");
        String returnedText = new String();
        if (values != null) {
            ArrayList<String> sheetLine = (ArrayList<String>) values.get(0);
            for (String s : sheetLine) {
                returnedText = returnedText + s.toString();
            }
        }
        return returnedText;
    }

    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }
}