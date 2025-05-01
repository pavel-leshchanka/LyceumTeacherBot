package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START;

@Slf4j
@Component
public class StartHandler extends Handler {
    public StartHandler(DialogAttributesService dialogAttributesService) {
        super(dialogAttributesService);
    }

    @Override
    DialogType getType() {
        return DialogType.START;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        return List.of(SendMessage.builder()
            .chatId(UpdateUtil.getChatId(update))
            .text(START)
            .build());
    }
}
