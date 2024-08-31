package by.faeton.lyceumteacherbot.controllers.handlers;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Handler {
    List<BotApiMethod> execute(Update update);

    boolean isAppropriateTypeMessage(Update update);
}
