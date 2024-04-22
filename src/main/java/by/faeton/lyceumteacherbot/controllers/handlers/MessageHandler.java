package by.faeton.lyceumteacherbot.controllers.handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface MessageHandler {
    List<SendMessage> execute(Update update);

    boolean isAppropriateTypeMessage(Update update);
}
