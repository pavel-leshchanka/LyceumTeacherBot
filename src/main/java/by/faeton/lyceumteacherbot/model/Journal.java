package by.faeton.lyceumteacherbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal {
    private Long id;
    private String journalId;
    private List<Student> students;
    private List<Teacher> teachers;
    private List<Subject> subjects;
    private String classParallel;
    private String classLetter;
    private String nameGUO;
    private Integer educationalYear;
    private Teacher classroomTeacher;
    private SchoolYearSchedule schoolYearSchedule;
    private ConsolidatedStatement consolidatedStatement;
    private List<SubjectSchedule> subjectSchedules;
}
