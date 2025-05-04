package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.services.BotService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.REFRESHED;

@Slf4j
@Component
public class RefreshHandler extends Handler {
    private final BotService botService;

    public RefreshHandler(
        BotService botService,
        DialogAttributesService dialogAttributesService
    ) {
        super(dialogAttributesService);
        this.botService = botService;
    }

    @Override
    DialogType getType() {
        return DialogType.REFRESH;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Long chatId = UpdateUtil.getChatId(update);
        botService.refreshContext();
        sendMessages.add(SendMessage.builder()
            .chatId(chatId)
            .text(REFRESHED)
            .build());
        return sendMessages;
    }
}
