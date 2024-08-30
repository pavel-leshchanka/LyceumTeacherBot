package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sheet-config.fields-name")
public record FieldsNameConfig(
        String journals,
        String students,
        String subjects,
        String schedule,
        String yearSchedule,
        String tasks,
        String consolidateStatement,
        String allStudents,
        String allTeachers,
        String absenteeismType
) {
}