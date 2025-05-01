package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.BotService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.REFRESHED;

@Slf4j
@Component
public class RefreshHandler extends Handler {
    private final UserRepository userRepository;
    private final BotService botService;

    public RefreshHandler(DialogAttributesService dialogAttributesService, UserRepository userRepository, BotService botService) {
        super(dialogAttributesService);
        this.userRepository = userRepository;
        this.botService = botService;
    }

    @Override
    DialogType getType() {
        return DialogType.REFRESH;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Long chatId = UpdateUtil.getChatId(update);
        userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                    botService.refreshContext();
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(REFRESHED)
                        .build());
                } else {
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(NO_ACCESS)
                        .build());
                }
            },
            () -> sendMessages.add(SendMessage.builder()
                .chatId(chatId)
                .text(NOT_AUTHORIZER)
                .build()));
        return sendMessages;
    }
}
