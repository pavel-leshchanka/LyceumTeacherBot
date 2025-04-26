package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sheet-config.fields-name")
public record FieldsNameConfig(
    String absenteeismType,
    String allStudents,
    String allTeachers,
    String consolidateStatement,
    String journals,
    String schedule,
    String students,
    String subjects,
    String tasks,
    String yearSchedule
) {
}
