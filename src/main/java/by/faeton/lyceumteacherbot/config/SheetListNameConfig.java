package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.sheet-lists")
public record SheetListNameConfig(
        String baseIdList,
        String settingsList,
        String studentsList
) {
}
