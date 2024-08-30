package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.utils.Logger;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
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
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final List<Handler> handlers;
    private final UserRepository userRepository;

    @Override
    public void onUpdateReceived(Update update) {
        List<List<BotApiMethod>> collect = handlers.stream()
                .filter(handler -> handler.isAppropriateTypeMessage(update))
                .map(handler -> handler.execute(update))
                .toList();
        if (collect.isEmpty()) {
            sendUserMessage(
                    SendMessage.builder()
                            .chatId(update.getCallbackQuery().getMessage().getChatId())
                            .text(ANOTHER_MESSAGES)
                            .build());
        } else if (collect.size() == 1 && collect.getFirst().isEmpty()) {
            sendUserMessage(SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text(ANOTHER_MESSAGES)
                    .build());
            Logger.log(update.getMessage().getChatId(), userRepository.findByTelegramId(update.getMessage().getChatId()).get(), update.getMessage().getText());
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
                log.info("User {} message arrived.", ((SendMessage) sendMessage).getChatId());
            }
            if (sendMessage instanceof EditMessageText) {
                log.info("User {} message arrived.", ((EditMessageText) sendMessage).getChatId());
            }
        } catch (TelegramApiException e) {
            if (sendMessage instanceof SendMessage) {
                log.warn("User {} message not arrived.", ((SendMessage) sendMessage).getChatId());
            }
            if (sendMessage instanceof EditMessageText) {
                log.warn("User {} message not arrived.", ((EditMessageText) sendMessage).getChatId());
            }
        }
    }
}