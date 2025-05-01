package by.faeton.lyceumteacherbot.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;

@UtilityClass
public class UpdateUtil {

    public static Long getChatId(Update update) {
        Long id;
        if (update.hasMessage()) {
            id = update.getMessage()
                .getChatId();
        } else if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery()
                .getMessage()
                .getChatId();
        } else {
            throw new RuntimeException();
        }
        return id;
    }
}
