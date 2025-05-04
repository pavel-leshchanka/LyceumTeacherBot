package by.faeton.lyceumteacherbot.utils;

import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.controllers.MessageSender;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
public class TelegramGoogleBrowser implements AuthorizationCodeInstalledApp.Browser {

    private final MessageSender messageSender;
    private final BotConfig botConfig;

    @Override
    public void browse(String url) {
        messageSender.sendUserMessage(SendMessage.builder()
            .chatId(botConfig.adminId())
            .text(url)
            .build());
    }
}
