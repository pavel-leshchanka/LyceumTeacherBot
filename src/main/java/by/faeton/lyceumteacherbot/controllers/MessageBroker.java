package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.utils.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker extends TelegramLongPollingBot {

    public static final String USER_MESSAGE_ARRIVED = "User {} message arrived.";
    public static final String USER_MESSAGE_NOT_ARRIVED = "User {} message not arrived.";
    private final BotConfig botConfig;
    private final List<Handler> handlers;
    private final UserRepository userRepository;

    @Override
    public void onUpdateReceived(Update update) {
        List<List<BotApiMethod>> collect = handlers.stream()
                .filter(handler -> handler.isAppropriateTypeMessage(update))
                .map(handler -> handler.execute(update))
                .toList();
        Long telegramId;
        String text;
        if (update.hasMessage()) {
            telegramId = update.getMessage().getChatId();
            text = update.getMessage().getText();
        } else if (update.hasCallbackQuery()) {
            telegramId = update.getCallbackQuery().getMessage().getChatId();
            text = update.getCallbackQuery().getMessage().getText();
        } else {
            throw new RuntimeException();
        }
        Optional<User> byTelegramId = userRepository.findByTelegramId(telegramId);

        if (collect.isEmpty()) {
            sendUserMessage(SendMessage.builder()
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .text(ANOTHER_MESSAGES)
                    .build());
            byTelegramId.ifPresentOrElse(byId -> Logger.log(telegramId, byId, text),
                    () -> Logger.log(telegramId, text));
        } else if (collect.size() == 1 && collect.getFirst().isEmpty()) {
            sendUserMessage(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(ANOTHER_MESSAGES)
                    .build());
            byTelegramId.ifPresentOrElse(byId -> Logger.log(telegramId, byId, text),
                    () -> Logger.log(telegramId, text));
        } else {
            collect.forEach(h -> h.forEach(this::sendUserMessage));
        }
    }

    public String getBotUsername() {
        return botConfig.botName();
    }

    public String getBotToken() {
        return botConfig.botToken();
    }

    public void sendUserMessage(BotApiMethod sendMessage) {
        try {
            execute(sendMessage);
            if (sendMessage instanceof SendMessage) {
                log.info(USER_MESSAGE_ARRIVED, ((SendMessage) sendMessage).getChatId());
            }
            if (sendMessage instanceof EditMessageText) {
                log.info(USER_MESSAGE_ARRIVED, ((EditMessageText) sendMessage).getChatId());
            }
        } catch (TelegramApiException e) {
            if (sendMessage instanceof SendMessage) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, ((SendMessage) sendMessage).getChatId());
            }
            if (sendMessage instanceof EditMessageText) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, ((EditMessageText) sendMessage).getChatId());
            }
        }
    }
}