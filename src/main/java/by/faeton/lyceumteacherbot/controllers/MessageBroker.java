package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.Logger;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final DialogAttributesService dialogAttributesService;
    private final BotConfig botConfig;
    private final List<Handler> handlers;
    private final UserRepository userRepository;
    private final MessageSender messageSender;

    @Override
    public void consume(Update update) {
        Long telegramId = UpdateUtil.getChatId(update);
        List<Handler> appropriateHandlers = handlers.stream()
            .filter(handler -> handler.isAppropriateTypeMessage(update))
            .toList();
        if (appropriateHandlers.size() == 1) {
            List<List<BotApiMethod>> messages = appropriateHandlers.stream()
                .map(handler -> handler.execute(update))
                .toList();
            messages.forEach(h -> h.forEach(messageSender::sendUserMessage));
        } else if (appropriateHandlers.size() > 1) {
            dialogAttributesService.remove(telegramId);
            messageSender.sendUserMessage(SendMessage.builder()
                .chatId(telegramId)
                .text("Диалогов больше одного. Начинаем сначала.")
                .build());
        } else {
            String text = update.hasMessage() ? update.getMessage().getText() : "No message.";
            Optional<User> byTelegramId = userRepository.findByTelegramId(telegramId);
            messageSender.sendUserMessage(SendMessage.builder()
                .chatId(telegramId)
                .text(ANOTHER_MESSAGES)
                .build());
            byTelegramId.ifPresentOrElse(byId -> Logger.log(telegramId, byId, text),
                () -> Logger.log(telegramId, text));
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
