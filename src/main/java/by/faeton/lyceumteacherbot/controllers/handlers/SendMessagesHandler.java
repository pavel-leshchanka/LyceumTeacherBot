package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.CommandHandler;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.SendMessagesDTO;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.SEX;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WHAT_SENDING;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IN_PROGRESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WRITING_IS_COMPLETED;

@Slf4j
@Component
public class SendMessagesHandler extends Handler {
    public static final String ALL_CALLBACK = "all";

    private final UserRepository userRepository;
    private final StudentsRepository studentsRepository;
    private final JournalRepository journalRepository;
    private final SchoolConfig schoolConfig;

    public SendMessagesHandler(DialogAttributesService dialogAttributesService, UserRepository userRepository, StudentsRepository studentsRepository, JournalRepository journalRepository, SchoolConfig schoolConfig) {
        super(dialogAttributesService);
        this.userRepository = userRepository;
        this.studentsRepository = studentsRepository;
        this.journalRepository = journalRepository;
        this.schoolConfig = schoolConfig;
    }

    @Override
    DialogType getType() {
        return DialogType.SEND_MESSAGE;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {

            SendMessagesDTO commandHandler = (SendMessagesDTO) dialogAttributesService.get(chatId);
            if (update.getMessage().getText().equals(DialogType.SEND_MESSAGE.getCommand()) && commandHandler == null) {
                SendMessagesDTO sendMessagesDTO = new SendMessagesDTO();
                userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                        if (user.getUserLevel().ordinal() >= UserLevel.ADMIN.ordinal()) {
                            Set<String> classParallels = journalRepository.getClassParallels();
                            HashSet<String> objects = new HashSet<>(classParallels);
                            objects.add(ALL_CALLBACK);
                            List<Pair<String, String>> list = objects.stream()
                                .map(e -> {
                                    sendMessagesDTO.setClassParallels(e);
                                    String s = generateJsonWithPrefix(sendMessagesDTO);
                                    return new Pair<>(e, s);
                                })
                                .toList();
                            sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                                CLASS_PARALLEL,
                                list
                            ));
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
                dialogAttributesService.remove(chatId);
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
                Set<String> classLetters = journalRepository.getClassLetters();
                HashSet<String> objects = new HashSet<>(classLetters);
                objects.add(ALL_CALLBACK);
                List<Pair<String, String>> list = objects.stream()
                    .map(e -> {
                        dtoFromCallback.setClassLetters(e);
                        String s = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(e, s);
                    }).toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                    CLASS_LETTER,
                    list
                ));
            } else if (dtoFromCallback.getSex() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(update.getCallbackQuery().getData())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> sex = studentsRepository.getAllStudentsSex();
                HashSet<String> objects = new HashSet<>(sex);
                objects.add(ALL_CALLBACK);
                List<Pair<String, String>> list = objects.stream()
                    .map(e -> {
                        dtoFromCallback.setSex(e);
                        String s = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(e, s);
                    }).toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                    SEX,
                    list
                ));
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
        List<SendMessage> sendMessages = new ArrayList<>();

        String classParallels = dialogAttribute.getClassParallels();
        String classLetters = dialogAttribute.getClassLetters();
        String sex = dialogAttribute.getSex();
        String text = dialogAttribute.getText();
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

        return sendMessages;
    }
}