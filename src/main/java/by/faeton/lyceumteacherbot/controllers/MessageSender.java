package by.faeton.lyceumteacherbot.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {
    public static final String USER_MESSAGE_ARRIVED = "User {} message arrived.";
    public static final String USER_MESSAGE_NOT_ARRIVED = "User {} message not arrived.";
    private final TelegramClient telegramClient;

    public void sendUserMessage(BotApiMethod<?> sendMessage) {
        try {
            telegramClient.execute(sendMessage);
            if (sendMessage instanceof SendMessage message) {
                log.info(USER_MESSAGE_ARRIVED, message.getChatId());
            }
            if (sendMessage instanceof EditMessageText messageText) {
                log.info(USER_MESSAGE_ARRIVED, messageText.getChatId());
            }
        } catch (TelegramApiException e) {
            log.error("", e);
            if (sendMessage instanceof SendMessage message) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, message.getChatId());
            }
            if (sendMessage instanceof EditMessageText editMessageText) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, editMessageText.getChatId());
            }
        }
    }
}
