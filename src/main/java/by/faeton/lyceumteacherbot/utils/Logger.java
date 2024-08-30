package by.faeton.lyceumteacherbot.utils;

import by.faeton.lyceumteacherbot.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Logger {
    private static SheetListener sheetListener;

    public Logger(SheetListener sheetListener) {
        Logger.sheetListener = sheetListener;
    }

    public static void log(Long chatId, User user, String message) {
        sheetListener.writeLog(List.of(List.of(chatId, user.getUserFirstName(), user.getUserLastName(), message)));
    }

    public static void log(Long chatId, String message) {
        sheetListener.writeLog(List.of(List.of(chatId, message)));
    }
}
