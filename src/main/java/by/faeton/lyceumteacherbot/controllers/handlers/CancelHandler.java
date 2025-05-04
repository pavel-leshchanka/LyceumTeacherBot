package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCELED;

@Component
public class CancelHandler extends Handler {
    private static final String CANCEL_CALLBACK = DialogType.CANCEL.getPrefix() + "Cancel";

    public CancelHandler(DialogAttributesService dialogAttributesService) {
        super(dialogAttributesService);
    }

    @Override
    DialogType getType() {
        return DialogType.CANCEL;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().equals(CANCEL_CALLBACK)) {
            dialogAttributesService.delete(chatId);
            EditMessageText build = EditMessageText.builder()
                .chatId(chatId)
                .text(CANCELED)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build();
            return List.of(build);
        }
        return List.of();
    }

    public static String getCancelCallback() {
        return CANCEL_CALLBACK;
    }
}
