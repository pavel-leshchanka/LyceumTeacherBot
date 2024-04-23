package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendingMessagesTextMessageHandler implements MessageHandler {

    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            return dialogAttributesService
                    .find(update.getMessage().getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<SendMessage> execute(Update update) {
        List<SendMessage> sendMessages = new ArrayList<>();
        Long chatId = update.getMessage().getChatId();
        dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
            userRepository.getAllUsers().forEach(user -> {
                sendMessages.add(SendMessage.builder()
                        .chatId(user.getTelegramUserId())
                        .text(update.getMessage().getText())
                        .build());
                dialogAttributesService.finalStep(chatId);
            });
        });
        return sendMessages;
    }
}