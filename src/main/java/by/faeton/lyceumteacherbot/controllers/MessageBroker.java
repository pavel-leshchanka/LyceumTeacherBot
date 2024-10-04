package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.utils.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;

@Slf4j
@Component
public class MessageBroker implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    public static final String USER_MESSAGE_ARRIVED = "User {} message arrived.";
    public static final String USER_MESSAGE_NOT_ARRIVED = "User {} message not arrived.";

    private final BotConfig botConfig;
    private final List<Handler> handlers;
    private final UserRepository userRepository;
    private final TelegramClient telegramClient;

    public MessageBroker(BotConfig botConfig, List<Handler> handlers, UserRepository userRepository) {
        this.telegramClient = new OkHttpTelegramClient(botConfig.botToken());
        this.botConfig = botConfig;
        this.handlers = handlers;
        this.userRepository = userRepository;
    }

    @Override
    public void consume(Update update) {
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
            text = update.getCallbackQuery().getData();
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
        } else if (collect.size() == 1 && collect.get(0).isEmpty()) {
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
            if (sendMessage instanceof SendMessage message) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, message.getChatId());
            }
            if (sendMessage instanceof EditMessageText editMessageText) {
                log.warn(USER_MESSAGE_NOT_ARRIVED, editMessageText.getChatId());
            }
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.botToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

}