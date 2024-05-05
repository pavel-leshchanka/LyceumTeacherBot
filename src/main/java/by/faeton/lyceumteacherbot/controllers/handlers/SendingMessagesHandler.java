package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WHAT_SENDING;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendingMessagesHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final StudentService studentService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                    .find(update.getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SEND_MESSAGE))
                    .orElse(false);
            return b || update.getMessage().getText().equals("/send_message");
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
            if (update.getMessage().getText().equals("/send_message") && dialogAttributesService.find(chatId).isEmpty()) {
                userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                            if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                                Set<String> classParallels = userRepository.getClassParallels();
                                classParallels.add("all");
                                sendMessages.add(getKeyboard(chatId,
                                        "Параллель",
                                        classParallels
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
                        Set<String> classLetters = userRepository.getClassLetters();
                        classLetters.add("all");
                        sendMessages.add(getKeyboard(chatId,
                                "Буква класса",
                                classLetters
                        ));
                    }
                    case 1 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        Set<String> sex = userRepository.getSex();
                        sex.add("all");
                        sendMessages.add(getKeyboard(chatId,
                                "Пол",
                                sex
                        ));

                    }
                    case 2 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        sendMessages.add(getKeyboard(chatId,
                                WHAT_SENDING,
                                Set.of("lab_nb", "test_nb")
                        ));
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
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (String s : callbackData) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(s)
                    .callbackData(s)
                    .build());
            rowsInline.add(row);
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Отмена")
                .callbackData("Cancel")
                .build());
        rowsInline.add(row);
        markupInline.setKeyboard(rowsInline);
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
            List<User> collect = userRepository.getAllUsers().stream()
                    .filter(u -> {
                        if (classParallels.equals("all")) {
                            return true;
                        } else {
                            return u.getClassParallel().equals(classParallels);
                        }
                    })
                    .filter(u -> {
                        if (classLetters.equals("all")) {
                            return true;
                        } else {
                            return u.getClassLetter().equals(classLetters);
                        }
                    })
                    .filter(u -> {
                        if (sex.equals("all")) {
                            return true;
                        } else {
                            return u.getSex().equals(sex);
                        }
                    })
                    .toList();
            if (text.equals("lab_nb")) {
                for (User user : collect) {
                    if (!studentService.isStudentLaboratoryNotebook(user)) {
                        sendMessages.add(SendMessage.builder()
                                .chatId(user.getTelegramUserId())
                                .text("Принеси тетрадь для лабораторных работ!")
                                .build());
                    }
                }
            } else if (text.equals("test_nb")) {
                for (User user : collect) {
                    if (!studentService.isStudentTestNotebook(user)) {
                        sendMessages.add(SendMessage.builder()
                                .chatId(user.getTelegramUserId())
                                .text("Принеси тетрадь для контрольных работ!")
                                .build());
                    }
                }
            } else {
                for (User user : collect) {
                    sendMessages.add(SendMessage.builder()
                            .chatId(user.getTelegramUserId())
                            .text(text)
                            .build());
                }
            }
        }
        return sendMessages;
    }
}