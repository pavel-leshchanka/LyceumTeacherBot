package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.SheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkingAbsenteeismCallbackQueryHandler implements MessageHandler {

    private final SheetService sheetService;
    private final DialogAttributesService dialogAttributesService;
    private final SchoolConfig schoolConfig;
    private final StudentsRepository studentsRepository;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.ABSENTEEISM))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        List<BotApiMethod> sendMessages = new ArrayList<>();
        dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
            switch (dialogAttribute.getStepOfDialog()) {
                case 0 -> {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text(studentsRepository.findByNumber(update.getCallbackQuery().getData()).get().getStudentName())
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                    sendMessages.add(getInlineKeyboardMarkup(update,
                        dialogAttribute,
                        START_ABSENTEEISM,
                        getClassesNumbers(schoolConfig.firstLesson(), schoolConfig.lastLesson())));}
                case 1 -> {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text("Начало пропуска: " + update.getCallbackQuery().getData())
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                    sendMessages.add(getInlineKeyboardMarkup(update,
                            dialogAttribute,
                            END_ABSENTEEISM,
                            getClassesNumbers(Integer.parseInt(update.getCallbackQuery().getData()), schoolConfig.lastLesson())));
                }
                case 2 -> {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text("Конец пропуска: " +update.getCallbackQuery().getData())
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                    sendMessages.add(getInlineKeyboardMarkup(update,
                            dialogAttribute,
                            TYPE_OF_ABSENTEEISM,
                            sheetService.getTypeAndValueOfAbsenteeism()));
                }
                case 3 -> {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text(sheetService.getTypeAndValueOfAbsenteeism().get(update.getCallbackQuery().getData()))
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                    dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                    sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(WRITING_IN_PROGRESS)
                            .build());
                    if (sheetService.writeAbsenteeism(dialogAttribute)) {
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