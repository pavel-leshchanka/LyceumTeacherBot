package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.UserRegisterDTO;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.REGISTER_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class RegisterHandler implements Handler {


    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;
    private final JournalRepository journalRepository;
    private final SchoolConfig schoolConfig;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                    .find(update.getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.REGISTER_NEW_USER))
                    .orElse(false);
            return b || update.getMessage().getText().equals(REGISTER_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.REGISTER_NEW_USER))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals(REGISTER_COMMAND) && dialogAttributesService.find(chatId).isEmpty()) {
                sendMessages.add(getKeyboard(chatId,
                        "Введите фамилию:",
                        new HashSet<>()
                ));
                dialogAttributesService.createDialog(DialogTypeStarted.REGISTER_NEW_USER, chatId);
            } else {
                dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                    if (dialogAttribute.getStepOfDialog().equals(0)) {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getMessage().getText())
                                .messageId(update.getMessage().getMessageId() - 1)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                        sendMessages.add(getKeyboard(chatId,
                                "Введите имя:",
                                new HashSet<>()
                        ));
                    } else if (dialogAttribute.getStepOfDialog().equals(1)) {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getMessage().getText())
                                .messageId(update.getMessage().getMessageId() - 1)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                        sendMessages.add(getKeyboard(chatId,
                                "Введите отчество:",
                                new HashSet<>()
                        ));
                    } else if (dialogAttribute.getStepOfDialog().equals(2)) {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getMessage().getText())
                                .messageId(update.getMessage().getMessageId() - 1)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                        sendMessages.add(getKeyboard(chatId,
                                "Введите пол:",
                                new HashSet<>()
                        ));
                    } else if (dialogAttribute.getStepOfDialog().equals(3)) {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getMessage().getText())
                                .messageId(update.getMessage().getMessageId() - 1)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                        sendMessages.add(getKeyboard(chatId,
                                "Введите класс:",
                                new HashSet<>()
                        ));
                    } else if (dialogAttribute.getStepOfDialog().equals(4)) {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getMessage().getText())
                                .messageId(update.getMessage().getMessageId() - 1)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                        sendMessages.add(getKeyboard(chatId,
                                "Выберете кем являетесь:",
                                Set.of("Учитель", "Ученик", "Родитель")
                        ));
                    }
                });
            }
        }
        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                if (dialogAttribute.getStepOfDialog() == 5) {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text(update.getCallbackQuery().getData())
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build());
                    dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                    dialogAttributesService.find(chatId).ifPresent(d -> {
                        List<String> receivedData = d.getReceivedData();
                        UserRegisterDTO build = UserRegisterDTO.builder()
                                .userLastName(receivedData.get(0))
                                .userFirstName(receivedData.get(1))
                                .userFatherName(receivedData.get(2))
                                .sex(receivedData.get(3))
                                .className(receivedData.get(4))
                                .userLevel(receivedData.get(5))
                                .telegramUserId(chatId)
                                .build();
                        userRepository.registerNewUser(build);
                        dialogAttributesService.deleteByTelegramId(chatId);
                    });
                }
            });
        }
        return sendMessages;
    }

    private SendMessage getKeyboard(Long chatId, String text, Set<String> callbackData) {
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        for (String s : callbackData) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(s)
                    .callbackData(s)
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
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