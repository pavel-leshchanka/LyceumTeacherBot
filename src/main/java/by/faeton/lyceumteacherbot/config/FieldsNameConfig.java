package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sheet-config.fields-name")
public record FieldsNameConfig(
        String laboratoryNotebookColumn,
        String testNotebookColumn,
        String startMarksColumn,
        String endMarksColumn,
        String startQuarterMarksColumn,
        String endQuarterMarksColumn,
        String dateField,
        String fieldTypeOfWork,
        String studentsFields,
        String typeOfAbsenteeism,
        Integer numberOfFirstColumnWithAbsenteeism) {

}
