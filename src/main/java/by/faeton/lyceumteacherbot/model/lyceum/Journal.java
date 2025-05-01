package by.faeton.lyceumteacherbot.model.lyceum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal {
    //  @Id
    //  @GeneratedValue(strategy = GenerationType.SEQUENCE)
    //  @Column(name = "id", nullable = false)
    private Long id;

    private String journalId;

    // @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Student> students;

    //  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Teacher> teachers;

    // @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Subject> subjects;

    private String classParallel;

    private String classLetter;

    private String nameGUO;

    private Integer educationalYear;

    //  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //  @JoinColumn(name = "teacher_id")
    private Teacher classroomTeacher;

    //  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    //  @JoinColumn(name = "school_year_schedule_id")
    private SchoolYearSchedule schoolYearSchedule;

    //  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ConsolidatedStatement consolidatedStatement;

    //  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SubjectSchedule> subjectSchedules;
}
