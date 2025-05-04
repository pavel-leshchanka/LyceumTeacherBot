package by.faeton.lyceumteacherbot.utils;

import by.faeton.lyceumteacherbot.controllers.handlers.CancelHandler;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;

@UtilityClass
public class KeyboardUtil {
    public static SendMessage getKeyboard(Long chatId, String text, List<Pair<String, String>> pairs, int colsCount) {
        List<List<Pair<String, String>>> groups = cutListToGroups(pairs, colsCount);
        List<InlineKeyboardRow> rowsInline = groups.stream()
            .map(group -> {
                List<InlineKeyboardButton> row = group.stream()
                    .map(pair -> (InlineKeyboardButton) InlineKeyboardButton.builder()
                        .text(pair.text())
                        .callbackData(pair.callbackData())
                        .build())
                    .toList();
                return new InlineKeyboardRow(row);
            })
            .collect(Collectors.toCollection(ArrayList::new));
        List<InlineKeyboardButton> cancelButton = List.of(
            InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CancelHandler.getCancelCallback())
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

    private static List<List<Pair<String, String>>> cutListToGroups(List<Pair<String, String>> pairs, int colsCount) {
        List<List<Pair<String, String>>> groups = new ArrayList<>();
        int size = pairs.size();
        for (int i = 0; i < size; i += colsCount) {
            List<Pair<String, String>> group = new ArrayList<>();
            for (int j = 0; j < colsCount && (i + j) < size; j++) {
                group.add(pairs.get(i + j));
            }
            groups.add(group);
        }
        return groups;
    }
}
