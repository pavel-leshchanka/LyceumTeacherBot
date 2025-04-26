package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCELED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    public static final String CANCEL_CALLBACK = "Cancel";


    private final DialogAttributesService dialogAttributesService;
    private final BotConfig botConfig;
    private final List<Handler> handlers;
    private final UserRepository userRepository;
    private final MessageSender messageSender;


    @Override
    public void consume(Update update) {
        Optional<List<BotApiMethod>> execute = execute(update);
        if (execute.isPresent()) {
            execute.get().forEach(messageSender::sendUserMessage);
        } else {

            List<Handler> appropriateHandlers = handlers.stream()
                .filter(handler -> handler.isAppropriateTypeMessage(update))
                .toList();
            if (appropriateHandlers.size() == 1) {
                List<List<BotApiMethod>> messages = appropriateHandlers.stream()
                    .map(handler -> handler.execute(update))
                    .toList();
                messages.forEach(h -> h.forEach(messageSender::sendUserMessage));
            } else {
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
                messageSender.sendUserMessage(SendMessage.builder()
                    .chatId(telegramId)
                    .text(ANOTHER_MESSAGES)
                    .build());
                byTelegramId.ifPresentOrElse(byId -> Logger.log(telegramId, byId, text),
                    () -> Logger.log(telegramId, text));
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


    public Optional<List<BotApiMethod>> execute(Update update) {
        List list = null;
        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (update.getCallbackQuery().getData().equals(CANCEL_CALLBACK) && dialogAttributesService.find(chatId).isPresent()) {
                dialogAttributesService.deleteByTelegramId(chatId);
                list = List.of(EditMessageText.builder()
                    .chatId(chatId)
                    .text(CANCELED)
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
            }
        }
        return Optional.ofNullable(list);
    }


}
