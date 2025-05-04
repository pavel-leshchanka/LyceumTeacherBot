package by.faeton.lyceumteacherbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "sheet-config.sheet-lists")
public record SheetListNameConfig(
    String journals,
    String students,
    String subjects,
    String schedule,
    String yearSchedule,
    String tasks,
    String firstQuarterNumbers,
    String secondQuarterNumbers,
    String threeQuarterNumbers,
    String fourQuarterNumbers,
    String yearNumbers,
    String examNumbers,
    String finalNumbers,
    String baseIdList,
    String allStudents,
    String allTeachers,
    String absenteeismType,
    String logsList
) {
}
