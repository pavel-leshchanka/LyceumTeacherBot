package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "school")
public record SchoolConfig(Integer firstLesson,
                           Integer lastLesson) {
}
