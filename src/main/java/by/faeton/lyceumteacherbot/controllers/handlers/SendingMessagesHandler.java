package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.UserLevel;
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

import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.ALL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.SEX;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WHAT_SENDING;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.SEND_MESSAGE_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendingMessagesHandler implements Handler {


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
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE))
                    .orElse(false);
            return b || update.getMessage().getText().equals(SEND_MESSAGE_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals(SEND_MESSAGE_COMMAND) && dialogAttributesService.find(chatId).isEmpty()) {
                userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                            if (user.getUserLevel().ordinal() >= UserLevel.ADMIN.ordinal()) {
                                Set<String> classParallels = journalRepository.getClassParallels();
                                HashSet<String> objects = new HashSet<>(classParallels);
                                objects.add(ALL_CALLBACK);
                                sendMessages.add(getKeyboard(chatId,
                                        CLASS_PARALLEL,
                                        objects
                                ));
                                dialogAttributesService.createDialog(DialogTypeStarted.SEND_MESSAGE, chatId);
                            } else {
                                sendMessages.add(SendMessage.builder()
                                        .chatId(chatId)
                                        .text(NO_ACCESS)
                                        .build());
                            }
                        },
                        () -> sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(NOT_AUTHORIZER)
                                .build()));
            }
            dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                if (dialogAttribute.getStepOfDialog().equals(3)) {
                    sendMessages.add(EditMessageText.builder()
                            .chatId(chatId)
                            .text(update.getMessage().getText())
                            .messageId(update.getMessage().getMessageId() - 1)
                            .build());
                    dialogAttributesService.nextStep(dialogAttribute, update.getMessage().getText());
                    sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(WRITING_IN_PROGRESS)
                            .build());
                    sendMessages.addAll(sendMessages(dialogAttribute));
                    sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(WRITING_IS_COMPLETED)
                            .build());
                    dialogAttributesService.deleteByTelegramId(chatId);
                }
            });
        }
        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                switch (dialogAttribute.getStepOfDialog()) {
                    case 0 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        Set<String> classLetters = journalRepository.getClassLetters();
                        HashSet<String> objects = new HashSet<>(classLetters);
                        objects.add(ALL_CALLBACK);
                        sendMessages.add(getKeyboard(chatId,
                                CLASS_LETTER,
                                objects
                        ));
                    }
                    case 1 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        Set<String> sex = studentsRepository.getAllStudentsSex();
                        HashSet<String> objects = new HashSet<>(sex);
                        objects.add(ALL_CALLBACK);
                        sendMessages.add(getKeyboard(chatId,
                                SEX,
                                objects
                        ));
                    }
                    case 2 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WHAT_SENDING)
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                    }
                    case 3 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IN_PROGRESS)
                                .build());
                        sendMessages.addAll(sendMessages(dialogAttribute));
                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(WRITING_IS_COMPLETED)
                                .build());
                        dialogAttributesService.deleteByTelegramId(chatId);
                    }
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

    public List<SendMessage> sendMessages(DialogAttribute dialogAttribute) {
        List<String> receivedData = dialogAttribute.getReceivedData();
        List<SendMessage> sendMessages = new ArrayList<>();
        if (receivedData.size() == 4) {
            String classParallels = receivedData.get(0);
            String classLetters = receivedData.get(1);
            String sex = receivedData.get(2);
            String text = receivedData.get(3);
            journalRepository.findAllByYear(schoolConfig.currentAcademicYear()).stream()
                    .filter(u -> {
                        if (classParallels.equals(ALL_CALLBACK)) {
                            return true;
                        } else {
                            return u.getClassParallel().equals(classParallels);
                        }
                    })
                    .filter(u -> {
                        if (classLetters.equals(ALL_CALLBACK)) {
                            return true;
                        } else {
                            return u.getClassLetter().equals(classLetters);
                        }
                    })
                    .flatMap(s -> s.getStudents().stream())
                    .filter(u -> {
                        if (sex.equals(ALL_CALLBACK)) {
                            return true;
                        } else {
                            return u.getSex().equals(sex);
                        }
                    })
                    .flatMap(student -> userRepository.findBySubjectOfEducationId(student.getStudentId()).stream())
                    .filter(user -> user.getUserLevel().equals(UserLevel.STUDENT))
                    .forEach(user -> sendMessages.add(SendMessage.builder()
                            .chatId(user.getTelegramUserId())
                            .text(text)
                            .build()));
        }
        return sendMessages;
    }
}