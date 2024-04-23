package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sheet-config.sheet-lists")
public record SheetListNameConfig(
        String baseIdList,
        String absenteeismList) {
}
