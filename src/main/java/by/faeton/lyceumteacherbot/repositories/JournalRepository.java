package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.ConsolidatedStatement;
import by.faeton.lyceumteacherbot.model.lyceum.ConsolidatedSubject;
import by.faeton.lyceumteacherbot.model.lyceum.Journal;
import by.faeton.lyceumteacherbot.model.lyceum.SchoolYearSchedule;
import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.model.lyceum.Subject;
import by.faeton.lyceumteacherbot.model.lyceum.SubjectNumber;
import by.faeton.lyceumteacherbot.model.lyceum.SubjectSchedule;
import by.faeton.lyceumteacherbot.model.lyceum.Task;
import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
import by.faeton.lyceumteacherbot.utils.addressgenerator.CellAddressGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JournalRepository {
    private static final String MASTER_SHEET_ID = "1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final SheetListener sheetListener;
    private final StudentsRepository studentsRepository;
    private final TeacherRepository teacherRepository;

    private final List<Journal> journals;


    @PostConstruct
    private void setUp() {
        //journals
        List<Journal> journalList = new ArrayList<>();
        List<List<String>> journalsListsStrings = sheetListener.getSheetList(MASTER_SHEET_ID, "journals", "A2:F100").orElseThrow();
        for (List<String> journal : journalsListsStrings) {
            String sheetId = journal.get(5);

            //students of journal
            List<Student> studentList = sheetListener.getSheetList(sheetId, "students", "B2:B31").orElseThrow().stream()
                    .map(strings -> studentsRepository.findByStudentId(strings.get(0)).orElseThrow())
                    .collect(Collectors.toCollection(ArrayList::new));

            //subjects and teachers
            Set<Teacher> teachers = new HashSet<>();
            List<Subject> subjects = sheetListener.getSheetList(sheetId, "subjects", "A2:C31").orElseThrow().stream()
                    .map(string -> {
                        Teacher byTeacherId = teacherRepository.findByTeacherId(string.get(2)).orElseThrow();
                        teachers.add(byTeacherId);
                        return Subject.builder()
                                .id(Long.parseLong(string.get(0)))
                                .name(string.get(1))
                                .teacher(byTeacherId)
                                .tasks(new ArrayList<>())
                                .build();
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            //subjectSchedule
            List<List<String>> subjectsScheduleList = sheetListener.getSheetList(sheetId, "schedule", "A1:H20").orElseThrow();
            List<SubjectSchedule> subjectSchedules = new ArrayList<>();
            for (int i = 1; i < 9; i++) {
                getSemesterShedule(subjectsScheduleList, 1, i, subjectSchedules, subjects);
            }
            for (int i = 11; i < 19; i++) {
                getSemesterShedule(subjectsScheduleList, 2, i - 10, subjectSchedules, subjects);
            }

            //schedule Year
            List<List<String>> schoolYearScheduleStr = sheetListener.getSheetList(sheetId, "yearSchedule", "A1:D6").orElseThrow();
            SchoolYearSchedule schoolYearSchedule = SchoolYearSchedule.builder()
                    .firstQuarterStart(LocalDate.parse(schoolYearScheduleStr.get(1).get(1), formatter))
                    .firstQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get(1).get(2), formatter))
                    .secondQuarterStart(LocalDate.parse(schoolYearScheduleStr.get(2).get(1), formatter))
                    .secondQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get(2).get(2), formatter))
                    .threeQuarterStart(LocalDate.parse(schoolYearScheduleStr.get(3).get(1), formatter))
                    .threeQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get(3).get(2), formatter))
                    .fourQuarterStart(LocalDate.parse(schoolYearScheduleStr.get(4).get(1), formatter))
                    .fourQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get(4).get(2), formatter))
                    .build();

            //consolidate statement
            ConsolidatedStatement consolidatedStatement = new ConsolidatedStatement();
            consolidatedStatement.setFirstQuarterNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "firstQuarterNumbers", subjects));
            consolidatedStatement.setSecondQuarterNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "secondQuarterNumbers", subjects));
            consolidatedStatement.setThreeQuarterNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "threeQuarterNumbers", subjects));
            consolidatedStatement.setFourQuarterNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "fourQuarterNumbers", subjects));
            consolidatedStatement.setYearNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "yearNumbers", subjects));
            consolidatedStatement.setExamNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "examNumbers", subjects));
            consolidatedStatement.setFinalNumbers(getNumbers(sheetListener, studentsRepository, sheetId, "finalNumbers", subjects));

            //tasks
            List<List<String>> tasks = sheetListener.getSheetList(sheetId, "tasks", "A1:CGZ38").orElseThrow();

            for (int i = 3; i < tasks.get(0).size() - 1; i++) {
                String subject = tasks.get(4).get(i);
                Optional<Subject> first = subjects.stream()
                        .filter(s -> s.getName().equals(subject))
                        .findFirst();
                if (first.isPresent()) {
                    Task task = Task.builder()
                            .taskId(CellAddressGenerator.convertNumberColumnToLetter(i + 1))
                            .date(LocalDate.of(
                                    Integer.valueOf(tasks.get(2).get(i)),
                                    Integer.valueOf(tasks.get(1).get(i)),
                                    Integer.valueOf(tasks.get(0).get(i))
                            ))
                            .themeName(tasks.get(5).get(i))
                            .homeWork(tasks.get(6).get(i))
                            .subjectNumbers(new ArrayList<>())
                            .taskNumber(Integer.valueOf(tasks.get(3).get(i)))
                            .build();
                    first.get().getTasks().add(task);
                    for (int j = 7; j < tasks.size() - 1; j++) {
                        Optional<Student> byStudentId = studentsRepository.findByStudentId(tasks.get(j).get(1));
                        String value = tasks.get(j).get(i);
                        if (byStudentId.isPresent() && !value.equals("")) {
                            SubjectNumber subjectNumber1 = SubjectNumber.builder()
                                    .id((long) (j + 1))
                                    .valuee(value)
                                    .student(byStudentId.get())
                                    .build();
                            task.getSubjectNumbers().add(subjectNumber1);
                        }
                    }
                }
            }

            Optional<Teacher> byTeacherId = teacherRepository.findByTeacherId(journal.get(4));

            byTeacherId.ifPresent(teacher -> journalList.add(
                    Journal.builder()
                            .journalId(journal.get(5))
                            .students(studentList)
                            .teachers(teachers.stream().toList())
                            .subjects(subjects)
                            .classParallel(journal.get(1))
                            .classLetter(journal.get(2))
                            .nameGUO(journal.get(3))
                            .classroomTeacher(teacher)
                            .schoolYearSchedule(schoolYearSchedule)
                            .consolidatedStatement(consolidatedStatement)
                            .subjectSchedules(subjectSchedules)
                            .build()));
        }
        journals.clear();
        journals.addAll(journalList);
    }

    private List<ConsolidatedSubject> getNumbers(SheetListener sheetListener, StudentsRepository studentsRepository, String sheetId, String sheetName, List<Subject> subjects) {
        List<List<String>> consStat = sheetListener.getSheetList(sheetId, sheetName, "A1:AC32").orElseThrow();
        List<ConsolidatedSubject> consolidatedSubjectList = new ArrayList<>();
        for (int i = 3; i < consStat.get(0).size() - 1; i++) {
            String subject = consStat.get(0).get(i);
            Optional<Subject> first = subjects.stream()
                    .filter(s -> s.getName().equals(subject))
                    .findFirst();
            if (first.isPresent()) {
                ConsolidatedSubject consolidatedSubject = new ConsolidatedSubject();
                consolidatedSubject.setSubject(first.get());
                consolidatedSubject.setSubjectNumber(new ArrayList<>());
                for (int j = 1; j < consStat.size() - 1; j++) {
                    String student = consStat.get(j).get(1);
                    Optional<Student> byStudentId = studentsRepository.findByStudentId(student);
                    if (byStudentId.isPresent()) {
                        String subjectNumber = consStat.get(j).get(i);
                        if (!subjectNumber.equals("")) {
                            SubjectNumber subjectNumber1 = SubjectNumber.builder()
                                    .valuee(subjectNumber)
                                    .student(byStudentId.get())
                                    .build();
                            consolidatedSubject.getSubjectNumber().add(subjectNumber1);
                            consolidatedSubject.setId((long) j + 1);
                            consolidatedSubjectList.add(consolidatedSubject);
                        }
                    }
                }
            }
        }
        return consolidatedSubjectList;
    }

    private static void getSemesterShedule(List<List<String>> subjectsScheduleList, int semester, int i, List<SubjectSchedule> subjectSchedules, List<Subject> subjects) {
        List<String> strings = subjectsScheduleList.get(i);
        for (int j = 1; j < strings.size() - 1; j++) {
            int a = j;
            if (!strings.get(a).equals("")) {
                subjectSchedules.add(SubjectSchedule.builder()
                        .semester(semester)
                        .dayOfWeek(DayOfWeek.of(j))
                        .subjectNumber(i)
                        .subject(subjects.stream()
                                .filter(subject -> subject.getName().equals(strings.get(a)))
                                .findFirst()
                                .orElseThrow())
                        .build());
            }
        }
    }
}
