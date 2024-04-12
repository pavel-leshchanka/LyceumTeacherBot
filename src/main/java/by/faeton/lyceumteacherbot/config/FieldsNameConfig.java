package by.faeton.lyceumteacherbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application.fields-name")
public class FieldsNameConfig {

    private String laboratoryNotebookColumn;
    private String testNotebookColumn;
    private String startMarksColumn;
    private String endMarksColumn;
    private String startQuarterMarksColumn;
    private String endQuarterMarksColumn;
    private String dateField;
    private String fieldTypeOfWork;

}
