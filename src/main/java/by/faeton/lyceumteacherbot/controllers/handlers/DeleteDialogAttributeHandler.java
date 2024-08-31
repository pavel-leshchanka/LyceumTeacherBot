package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCELED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteDialogAttributeHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals(CANCEL_CALLBACK)) {
                return dialogAttributesService.find(update.getCallbackQuery().getMessage().getChatId()).isPresent();
            }
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        dialogAttributesService.deleteByTelegramId(chatId);
        return List.of(EditMessageText.builder()
                .chatId(chatId)
                .text(CANCELED)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build());
    }
}