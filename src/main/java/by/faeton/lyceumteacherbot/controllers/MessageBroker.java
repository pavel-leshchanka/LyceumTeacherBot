package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<MessageHandler> handlers;

    @Override
    public void onUpdateReceived(Update update) {
        handlers.stream()
                .filter(handler -> handler.isAppropriateTypeMessage(update))
                .forEach(handler -> handler.execute(update).forEach(this::sendUserMessage));
    }

    public String getBotUsername() {
        return botConfig.botName();
    }

    public String getBotToken() {
        return botConfig.botToken();
    }

    public void sendUserMessage(BotApiMethod sendMessage) {
        try {
           // String name = sendMessage.getClass().getName();
            execute(sendMessage);
           // log.info("User {} message arrived.", sendMessage.getChatId());
        } catch (TelegramApiException e) {
         //   log.warn("User {} message not arrived.", sendMessage.getChatId());
        }
    }
}