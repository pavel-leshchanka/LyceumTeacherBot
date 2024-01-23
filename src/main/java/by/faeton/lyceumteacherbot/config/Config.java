package by.faeton.lyceumteacherbot.config;

import by.faeton.lyceumteacherbot.services.UserService;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Optional;

@Configuration
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    @Bean
    public UserService setUp(SheetListener sheetListener, BotConfig botConfig) {
        UserService userService = new UserService();
        Optional<ArrayList<ArrayList<String>>> values = sheetListener.getSheetList(botConfig.getSettingsList());
        if (values.isPresent()) {
            for (ArrayList<String> value : values.get()) {
                switch (value.get(0)) {
                    case "laboratoryNotebookColumn" -> userService.setLaboratoryNotebookColumn(value.get(1));
                    case "testNotebookColumn" -> userService.setTestNotebookColumn(value.get(1));
                    case "startMarksColumn" -> userService.setStartMarksColumn(value.get(1));
                    case "endMarksColumn" -> userService.setEndMarksColumn(value.get(1));
                    case "dateField" -> userService.setDateField(value.get(1));
                    case "fieldTypeOfWork" -> userService.setFieldTypeOfWork(value.get(1));
                    case "startQuarterMarksColumn" -> userService.setStartQuarterMarksColumn(value.get(1));
                    case "endQuarterMarksColumn" -> userService.setEndQuarterMarksColumn(value.get(1));
                    default -> log.warn("Key " + value.get(0) + "not fount.");
                }
            }
        }
        log.info("User Service is configured");
        return userService;
    }
}
