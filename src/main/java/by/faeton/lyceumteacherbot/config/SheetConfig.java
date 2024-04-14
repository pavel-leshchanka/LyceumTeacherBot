package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "url")
public record SheetConfig(
        String sheetId) {
}
