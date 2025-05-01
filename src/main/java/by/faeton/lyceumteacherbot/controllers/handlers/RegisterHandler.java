package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.RegisterDTO;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RegisterHandler extends Handler {

    private final UserRepository userRepository;

    public RegisterHandler(DialogAttributesService dialogAttributesService, UserRepository userRepository) {
        super(dialogAttributesService);
        this.userRepository = userRepository;
    }

    @Override
    DialogType getType() {
        return DialogType.REGISTER;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Long chatId = UpdateUtil.getChatId(update);
        RegisterDTO commandHandler = (RegisterDTO) dialogAttributesService.get(chatId);
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals(DialogType.REGISTER.getCommand()) && commandHandler == null) {
                sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                    "Введите фамилию:",
                    List.of()
                ));
                dialogAttributesService.save(chatId, new RegisterDTO());
            } else {
                if (commandHandler != null && commandHandler.getUserLastName() == null) {
                    commandHandler.setUserLastName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    dialogAttributesService.save(chatId, commandHandler);
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        "Введите имя:",
                        List.of()
                    ));
                } else if (commandHandler != null && commandHandler.getUserFirstName() == null) {
                    commandHandler.setUserFirstName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    dialogAttributesService.save(chatId, commandHandler);
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        "Введите отчество:",
                        List.of()
                    ));
                } else if (commandHandler != null && commandHandler.getUserFatherName() == null) {
                    commandHandler.setUserFatherName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    dialogAttributesService.save(chatId, commandHandler);
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        "Введите пол:",
                        List.of()
                    ));
                } else if (commandHandler != null && commandHandler.getSex() == null) {
                    commandHandler.setSex(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        "Введите класс:",
                        List.of()
                    ));
                } else if (commandHandler != null && commandHandler.getClassName() == null) {
                    commandHandler.setClassName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId,
                        "Выберете кем являетесь:\"Учитель\", \"Ученик\", \"Родитель\"",
                        List.of()
                    ));
                } else if (commandHandler != null && commandHandler.getUserLevel() == null) {
                    commandHandler.setUserLevel(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    extracted(commandHandler, chatId);
                    dialogAttributesService.remove(chatId);
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text("Ожидайте.")
                        .build());
                }
            }
        }
        return sendMessages;
    }

    private void extracted(RegisterDTO dtoFromCallback, Long chatId) {
        userRepository.registerNewUser(dtoFromCallback, chatId);
    }
}