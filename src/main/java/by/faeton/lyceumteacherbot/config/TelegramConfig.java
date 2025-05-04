package by.faeton.lyceumteacherbot.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@RequiredArgsConstructor
public class TelegramConfig {
    private final BotConfig botConfig;

    @Bean
    public TelegramClient getTelegramClient() {
        return new OkHttpTelegramClient(botConfig.botToken());
    }
}
