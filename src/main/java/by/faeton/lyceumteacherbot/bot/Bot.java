package by.faeton.lyceumteacherbot.bot;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.SheetListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@RequiredArgsConstructor
public class Bot extends TelegramLongPollingBot {
    @Autowired
    private final BotConfig botConfig;
    @Autowired
    private final SheetListener sheetListener;



    @Override
    public void onUpdateReceived(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String receivedMessage = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String sheetText = sheetListener.getStudentMarks(chatId);
        sendMessage.setText(sheetText+receivedMessage);
        try {
            this.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }
}