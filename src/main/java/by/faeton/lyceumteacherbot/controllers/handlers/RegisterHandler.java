package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.RegisterDTO;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.TelegramUserService;
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

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_CLASS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_EDUCATION_ROLE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_FATHERNAME;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_LASTNAME;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_NAME;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ENTER_SEX;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.WAITING;

@Slf4j
@Component
public class RegisterHandler extends Handler {
    private final TelegramUserService userService;

    public RegisterHandler(
        DialogAttributesService dialogAttributesService,
        TelegramUserService userService
    ) {
        super(dialogAttributesService);
        this.userService = userService;
    }

    @Override
    DialogType getType() {
        return DialogType.REGISTER;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Long chatId = UpdateUtil.getChatId(update);
        RegisterDTO commandHandler = (RegisterDTO) dialogAttributesService.find(chatId);
        if (update.hasMessage()) {
            if (update.getMessage().getText().equals(DialogType.REGISTER.getCommand()) && commandHandler == null) {
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_LASTNAME, List.of(), 1));
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
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_NAME, List.of(), 1));
                } else if (commandHandler != null && commandHandler.getUserFirstName() == null) {
                    commandHandler.setUserFirstName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    dialogAttributesService.save(chatId, commandHandler);
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_FATHERNAME, List.of(), 1));
                } else if (commandHandler != null && commandHandler.getUserFatherName() == null) {
                    commandHandler.setUserFatherName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    dialogAttributesService.save(chatId, commandHandler);
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_SEX, List.of(), 1));
                } else if (commandHandler != null && commandHandler.getSex() == null) {
                    commandHandler.setSex(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_CLASS, List.of(), 1));
                } else if (commandHandler != null && commandHandler.getClassName() == null) {
                    commandHandler.setClassName(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, ENTER_EDUCATION_ROLE, List.of(), 1));
                } else if (commandHandler != null && commandHandler.getUserLevel() == null) {
                    commandHandler.setUserLevel(update.getMessage().getText());
                    sendMessages.add(EditMessageText.builder()
                        .chatId(chatId)
                        .text(update.getMessage().getText())
                        .messageId(update.getMessage().getMessageId() - 1)
                        .build());
                    userService.createNewUser(commandHandler, chatId);
                    sendMessages.add(SendMessage.builder()
                        .chatId(chatId)
                        .text(WAITING)
                        .build());
                    dialogAttributesService.delete(chatId);
                }
            }
        }
        return sendMessages;
    }
}
