package by.faeton.lyceumteacherbot.controllers;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.Handler;
import by.faeton.lyceumteacherbot.exceptions.ResourceNotFoundException;
import by.faeton.lyceumteacherbot.security.TelegramUserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.Logger;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ANOTHER_MESSAGES;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCELED;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_MESSAGE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TOO_MATCH_DIALOG_STARTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageBroker implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final AuthenticationProvider authenticationManager;
    private final BotConfig botConfig;
    private final DialogAttributesService dialogAttributesService;
    private final List<Handler> handlers;
    private final MessageSender messageSender;
    private final TelegramUserRepository userRepository;

    @Override
    public void consume(Update update) {
        Long telegramId = UpdateUtil.getChatId(update);
        authenticate(telegramId.toString());
        List<Handler> appropriateHandlers = handlers.stream()
            .filter(handler -> handler.isAppropriateTypeMessage(update))
            .toList();
        if (appropriateHandlers.size() == 1) {
            try {
                List<List<BotApiMethod>> messages = appropriateHandlers.stream()
                    .map(handler -> handler.execute(update))
                    .toList();
                messages.forEach(message -> message.forEach(messageSender::sendUserMessage));
            } catch (AuthorizationDeniedException e) {
                messageSender.sendUserMessage(SendMessage.builder()
                    .chatId(telegramId)
                    .text(NO_ACCESS)
                    .build());
                log.warn(NO_ACCESS, e);
            } catch (ResourceNotFoundException e) {
                if (update.hasCallbackQuery()) {
                    EditMessageText.builder()
                        .chatId(telegramId)
                        .text(CANCELED)
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .build();
                }
                messageSender.sendUserMessage(SendMessage.builder()
                    .chatId(telegramId)
                    .text(e.getMessage())
                    .build());
                log.warn(e.getMessage(), e);
            }
        } else if (appropriateHandlers.size() > 1) {
            dialogAttributesService.delete(telegramId);
            messageSender.sendUserMessage(SendMessage.builder()
                .chatId(telegramId)
                .text(TOO_MATCH_DIALOG_STARTED)
                .build());
        } else {
            String text = update.hasMessage() ? update.getMessage().getText() : NO_MESSAGE;
            messageSender.sendUserMessage(SendMessage.builder()
                .chatId(telegramId)
                .text(ANOTHER_MESSAGES)
                .build());
            userRepository.findByTelegramId(telegramId)
                .ifPresentOrElse(byId -> Logger.log(telegramId, byId, text),
                    () -> Logger.log(telegramId, text));
        }
    }

    private void authenticate(String username) {
        try {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, "");
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            log.error("", e);
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
