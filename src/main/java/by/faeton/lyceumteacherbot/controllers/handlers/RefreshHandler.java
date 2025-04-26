package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.BotService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.REFRESHED;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.REFRESH_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefreshHandler implements Handler {
    private final DialogAttributesService dialogAttributesService;

    private final UserRepository userRepository;
    private final BotService botService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {

        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.REFRESH))
                .orElse(false);
            return b || update.getMessage().getText().equals(REFRESH_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.REFRESH))
                .orElse(false);
        }
        return false;

    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
        optionalUser.ifPresentOrElse(user -> {
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