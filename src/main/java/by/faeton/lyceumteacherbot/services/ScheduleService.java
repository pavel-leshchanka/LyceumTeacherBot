package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.ScheduleDTO;
import by.faeton.lyceumteacherbot.model.ScheduleDays;
import by.faeton.lyceumteacherbot.model.Semester;
import by.faeton.lyceumteacherbot.model.SubjectSchedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;

    public String getText(ScheduleDTO dtoFromCallback) {
        ScheduleDays days = ScheduleDays.valueOf(dtoFromCallback.getDay());
        List<SubjectSchedule> subjectSchedules = journalService.getSchedule(dtoFromCallback.getClassParallel(), dtoFromCallback.getClassLetter(), schoolConfig.currentAcademicYear());
        Semester semester = Semester.valueOf(dtoFromCallback.getSemester());
        Map<DayOfWeek, List<SubjectSchedule>> collect = subjectSchedules.stream()
            .filter(subjectSchedule -> subjectSchedule.getSemester().equals(semester.getSemesterNumber()))
            .filter(subjectSchedule -> switch (days) {
                case ALL -> true;
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY ->
                    days.ordinal() == subjectSchedule.getDayOfWeek().ordinal();
                case TODAY -> LocalDateTime.now().getDayOfWeek().equals(subjectSchedule.getDayOfWeek());
            })
            .collect(Collectors.groupingBy(SubjectSchedule::getDayOfWeek));

        String collect1 = Arrays.stream(DayOfWeek.values())
            .map(e -> {
                if (collect.get(e) != null) {
                    return getDateLine(collect.get(e), e);
                } else {
                    return "";
                }
            }).collect(Collectors.joining());
        collect1 = collect1.isBlank() ? "Нет занятий" : collect1;
        return collect1;
    }

    private String getDateLine(List<SubjectSchedule> schedules, DayOfWeek dayOfWeek) {
        String d = switch (dayOfWeek) {
            case MONDAY -> "Понедельник";
            case TUESDAY -> "Вторник";
            case WEDNESDAY -> "Среда";
            case THURSDAY -> "Четверг";
            case FRIDAY -> "Пятница";
            case SATURDAY -> "Суббота";
            case SUNDAY -> "Воскресенье";
        };
        String collect = schedules.stream().sorted(Comparator.comparing(SubjectSchedule::getSubjectNumber))
            .map(a -> a.getSubjectNumber() + " " + a.getSubject().getName() + "\n")
            .collect(Collectors.joining());
        return d + "\n" + collect + "\n";
    }
}
