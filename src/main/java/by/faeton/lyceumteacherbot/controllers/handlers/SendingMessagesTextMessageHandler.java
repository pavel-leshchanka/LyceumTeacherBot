package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendingMessagesTextMessageHandler implements MessageHandler {

    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Optional<DialogAttribute> byId = dialogAttributesService.find(update.getMessage().getChatId());
            return update.getMessage().hasText() && byId.isPresent();
        }
        return false;
    }

    @Override
    public List<SendMessage> execute(Update update) {
        List<SendMessage> sendMessages = new ArrayList<>();
        String chatId = update.getMessage().getChatId().toString();
        dialogAttributesService.find(Long.valueOf(chatId)).ifPresent(us -> {
            if (us.getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE)) {
                userRepository.getAllUsers().forEach(user -> {
                    sendMessages.add(SendMessage.builder()
                            .chatId(user.getTelegramUserId())
                            .text(update.getMessage().getText())
                            .build());
                    dialogAttributesService.finalStep(Long.valueOf(chatId));
                });
            }
        });
        return sendMessages;
    }
}