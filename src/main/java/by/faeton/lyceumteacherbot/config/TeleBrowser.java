package by.faeton.lyceumteacherbot.config;

import by.faeton.lyceumteacherbot.controllers.MessageSender;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.IOException;

@Primary
@Component
@RequiredArgsConstructor
public class TeleBrowser implements AuthorizationCodeInstalledApp.Browser {

    private final MessageSender messageSender;

    @Override
    public void browse(String url) throws IOException {
        messageSender.sendUserMessage(SendMessage.builder()
            .chatId("456302481")
            .text(url)
            .build());
    }
}
