package by.faeton.lyceumteacherbot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BotConfig {

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${url.firstPart}")
    private String firstPart;

    @Value("${url.sheetId}")
    private String sheetId;

    @Value("${url.apiKey}")
    private String apiKey;

    @Value("${baseIdList}")
    private String baseIdList;

    @Value("${settingsList}")
    private String settingsList;

}
