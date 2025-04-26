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

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.HELP;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.HELP_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class HelpHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        Boolean isDialogStarted = dialogAttributesService
            .find(getChatId(update))
            .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.HELP))
            .orElse(false);
        boolean isMessageCommand = update.hasMessage() && update.getMessage().getText().equals(HELP_COMMAND);
        return isDialogStarted || isMessageCommand;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        sendMessages.add(SendMessage.builder()
            .chatId(chatId)
            .text(HELP)
            .build());
        return sendMessages;
    }

}
