package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@ConfigurationProperties(prefix = "bot")
public record BotConfig(
    Integer port,
    String botName,
    String botToken,
    String credentialsFilePath,
    String tokensFolder
) {
}
