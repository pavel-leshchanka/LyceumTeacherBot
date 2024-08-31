package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotConfig(
        String botName,
        String botToken,
        String tokensFolder,
        String credentialsFilePath,
        Integer port
) {
}