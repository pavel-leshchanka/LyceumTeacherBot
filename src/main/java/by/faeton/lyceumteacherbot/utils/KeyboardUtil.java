package by.faeton.lyceumteacherbot.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.controllers.handlers.CancelHandler.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;

public class KeyboardUtil {
    public static SendMessage getKeyboard(Long chatId, String text, List<Pair<String, String>> pairs) {
        List<InlineKeyboardRow> rowsInline = pairs.stream()
            .map(pair -> {
                List<InlineKeyboardButton> row = List.of(
                    InlineKeyboardButton.builder()
                        .text(pair.text())
                        .callbackData(pair.callbackData())
                        .build()
                );
                return new InlineKeyboardRow(row);
            })
            .collect(Collectors.toCollection(ArrayList::new));

        List<InlineKeyboardButton> cancelButton = List.of(
            InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build()
        );
        rowsInline.add(new InlineKeyboardRow(cancelButton));
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
            .keyboard(rowsInline)
            .build();
        return SendMessage.builder()
            .chatId(chatId)
            .replyMarkup(markupInline)
            .text(text)
            .build();
    }
}
