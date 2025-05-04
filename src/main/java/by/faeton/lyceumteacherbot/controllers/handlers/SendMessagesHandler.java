package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.SendMessagesDTO;
import by.faeton.lyceumteacherbot.security.UserLevel;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.services.TelegramUserService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.SEX;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WHAT_SENDING;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;

@Slf4j
@Component
public class SendMessagesHandler extends Handler {
    public static final String ALL_CALLBACK = "all";

    private final JournalService journalService;
    private final TelegramUserService userService;

    public SendMessagesHandler(
        DialogAttributesService dialogAttributesService,
        JournalService journalService,
        TelegramUserService userService
    ) {
        super(dialogAttributesService);
        this.journalService = journalService;
        this.userService = userService;
    }

    @Override
    DialogType getType() {
        return DialogType.SEND_MESSAGE;
    }

    @Override
    @PreAuthorize("hasAuthority('TEACHER')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            SendMessagesDTO commandHandler = (SendMessagesDTO) dialogAttributesService.find(chatId);
            if (update.getMessage().getText().equals(DialogType.SEND_MESSAGE.getCommand()) && commandHandler == null) {
                SendMessagesDTO sendMessagesDTO = new SendMessagesDTO();

                Set<String> classParallels = journalService.getClassParallels();
                HashSet<String> objects = new HashSet<>(classParallels);
                objects.add(ALL_CALLBACK);
                List<Pair<String, String>> pairList = objects.stream()
                    .map(parallel -> {
                        sendMessagesDTO.setClassParallels(parallel);
                        String jsonWithPrefix = generateJsonWithPrefix(sendMessagesDTO);
                        return new Pair<>(parallel, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_PARALLEL, pairList, 4));
            }
            if (commandHandler != null && commandHandler.getText() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(update.getMessage().getText())
                    .messageId(update.getMessage().getMessageId() - 1)
                    .build());
                commandHandler.setText(update.getMessage().getText());
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(WRITING_IN_PROGRESS)
                    .build());
                sendMessages.addAll(sendMessages(commandHandler));
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(WRITING_IS_COMPLETED)
                    .build());
                dialogAttributesService.delete(chatId);
            }
        }
        if (update.hasCallbackQuery()) {
            SendMessagesDTO dtoFromCallback = getDTOFromCallback(update, SendMessagesDTO.class);
            if (dtoFromCallback.getClassLetters() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(update.getCallbackQuery().getData())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> classLetters = journalService.getClassLetters();
                HashSet<String> objects = new HashSet<>(classLetters);
                objects.add(ALL_CALLBACK);
                List<Pair<String, String>> list = objects.stream()
                    .map(letter -> {
                        dtoFromCallback.setClassLetters(letter);
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(letter, jsonWithPrefix);
                    }).toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_LETTER, list, 4));
            } else if (dtoFromCallback.getSex() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(update.getCallbackQuery().getData())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> sexes = journalService.getAllStudentsSex();
                HashSet<String> objects = new HashSet<>(sexes);
                objects.add(ALL_CALLBACK);
                List<Pair<String, String>> list = objects.stream()
                    .map(sex -> {
                        dtoFromCallback.setSex(sex);
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(sex, jsonWithPrefix);
                    }).toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, SEX, list, 4));
            } else if (dtoFromCallback.getText() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(update.getCallbackQuery().getData())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(WHAT_SENDING)
                    .build());
                dialogAttributesService.save(chatId, dtoFromCallback);
            }
        }
        return sendMessages;
    }

    public List<SendMessage> sendMessages(SendMessagesDTO dialogAttribute) {
        String text = dialogAttribute.getText();
        return journalService.findUsersByParameters(dialogAttribute).stream()
            .flatMap(student -> userService.findBySubjectOfEducationId(student.getStudentId()).stream())
            .filter(user -> user.getUserLevel().equals(UserLevel.STUDENT))
            .map(user -> (SendMessage) SendMessage.builder()
                .chatId(user.getTelegramUserId())
                .text(text)
                .build())
            .toList();
    }
}