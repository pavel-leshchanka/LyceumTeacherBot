package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
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

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.START_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class StartMessageHandler implements Handler {
    private final DialogAttributesService dialogAttributesService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {

        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.START))
                .orElse(false);
            return b || update.getMessage().getText().equals(START_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.START))
                .orElse(false);
        }
        return false;

    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        sendMessages.add(SendMessage.builder()
            .chatId(chatId)
            .text(START)
            .build());
        return sendMessages;
    }

}