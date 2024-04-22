package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.SheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler implements MessageHandler {

    private final SheetService sheetService;
    private final DialogAttributesService dialogAttributesService;
    private final SchoolConfig schoolConfig;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        return update.hasCallbackQuery();
    }

    @Override
    public List<SendMessage> execute(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        List<SendMessage> sendMessages = new ArrayList<>();
        dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
            if (dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.ABSENTEEISM)) {
                if (dialogAttribute.getStepOfDialog() == 0) {
                    SendMessage sendMessage = getInlineKeyboardMarkup(update,
                            dialogAttribute,
                            START_ABSENTEEISM,
                            getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson()));
                    sendMessages.add(sendMessage);
                } else if (dialogAttribute.getStepOfDialog() == 1) {
                    SendMessage sendMessage = getInlineKeyboardMarkup(update,
                            dialogAttribute,
                            END_ABSENTEEISM,
                            getClassesNumbers(Integer.parseInt(update.getCallbackQuery().getData()), schoolConfig.lastLesson()));
                    sendMessages.add(sendMessage);
                } else if (dialogAttribute.getStepOfDialog() == 2) {
                    SendMessage sendMessage = getInlineKeyboardMarkup(update,
                            dialogAttribute,
                            TYPE_OF_ABSENTEEISM,
                            sheetService.getTypeAndValueOfAbsenteeism());
                    sendMessages.add(sendMessage);
                } else if (dialogAttribute.getStepOfDialog() == 3) {
                    dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                    sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(WRITING_IN_PROGRESS)
                            .build());
                    boolean isWritten = sheetService.writeAbsenteeism(dialogAttribute);
                    if (isWritten) {
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IS_COMPLETED)
                                .build());
                    } else {
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IS_NOT_COMPLETED)
                                .build());
                    }
                    dialogAttributesService.finalStep(chatId);
                }
            }
        });
        return sendMessages;
    }

    private SendMessage getInlineKeyboardMarkup(Update update, DialogAttribute dialogAttribute, String text, Map<String, String> map) {
        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        map.forEach((key, value) -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(value)
                    .callbackData(key)
                    .build());
            rowsInline.add(row);
        });
        markupInline.setKeyboard(rowsInline);
        return SendMessage.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private SendMessage getInlineKeyboardMarkup(Update update, DialogAttribute dialogAttribute, String text, List<String> callbackData) {
        return getInlineKeyboardMarkup(update, dialogAttribute, text, callbackData, callbackData);
    }

    private SendMessage getInlineKeyboardMarkup(Update update, DialogAttribute dialogAttribute, String text, List<String> callbackData, List<String> labels) {
        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (int i = 0; i < callbackData.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(labels.get(i)))
                    .callbackData(String.valueOf(callbackData.get(i)))
                    .build());
            rowsInline.add(row);
        }
        markupInline.setKeyboard(rowsInline);
        return SendMessage.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }

    private List<String> getClassesNumbers(Integer startClass, Integer endClass) {
        List<String> numbers = new ArrayList<>();
        if (endClass >= startClass) {
            for (int i = startClass; i <= endClass; i++) {
                numbers.add(String.valueOf(i));
            }
        }
        return numbers;
    }
}