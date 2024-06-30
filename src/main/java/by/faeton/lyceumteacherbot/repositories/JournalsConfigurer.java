package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.Journal;
import by.faeton.lyceumteacherbot.model.lyceum.SchoolYearSchedule;
import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.model.lyceum.Subject;
import by.faeton.lyceumteacherbot.model.lyceum.SubjectSchedule;
import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class JournalsConfigurer {
    private final SheetListener sheetListener;
    private final StudentsRepository studentsRepository;
    private final TeacherRepository teacherRepository;
    private final DialogAttributeRepository dialogAttributeRepository;
    private final JournalRepository journalRepository;

    @PostConstruct
    private void setUp() {
        //students
        List<Student> students = new ArrayList<>();
        Optional<List<List<String>>> studentsList = sheetListener.getSheetList("1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c", "students", "A2:F100");
        for (List<String> strings : studentsList.get()) {
            students.add(Student.builder()
                    .studentId(strings.get(0))
                    .userFirstName(strings.get(1))
                    .userLastName(strings.get(2))
                    .userFatherName(strings.get(3))
                    .sex(strings.get(4))
                    .build());
        }
        studentsRepository.setStudents(students);
        //teachers
        List<Teacher> teachers = new ArrayList<>();
        Optional<List<List<String>>> teachersList = sheetListener.getSheetList("1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c", "teachers", "A2:F100");
        for (List<String> strings : teachersList.get()) {
            teachers.add(Teacher.builder()
                    .teacherId(strings.get(0))
                    .name(strings.get(1))
                    .build());
        }
        teacherRepository.setTeachers(teachers);

        List<Journal> journalList = new ArrayList<>();
        Optional<List<List<String>>> journals = sheetListener.getSheetList("1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c", "journals", "A2:F100");
        for (List<String> journal : journals.get()) {
            //students
            Optional<List<List<String>>> studentsStr = sheetListener.getSheetList(journal.get(5), "students", "A1:A30");
            List<Student> studentList = new ArrayList<>();
            for (List<String> string : studentsStr.get()) {
                studentList.add(studentsRepository.findByStudentId(string.get(0)));
            }

            //subjects and teachers
            Set<Teacher> teachers1 = new HashSet<>();
            Optional<List<List<String>>> subjectsList = sheetListener.getSheetList(journal.get(5), "subjects", "A2:F100");
            List<Subject> subjects = new ArrayList<>();
            for (List<String> string : subjectsList.get()) {
                subjects.add(Subject.builder()
                        .id(Long.parseLong(string.get(0)))
                        .name(string.get(1))
                        .teacher(teacherRepository.findByTeacherId(string.get(2)))
                        .build());
                teachers1.add(teacherRepository.findByTeacherId(string.get(2)));
            }

            //subjectSchedule
            Optional<List<List<String>>> subjectsScheduleList = sheetListener.getSheetList(journal.get(5), "schedule", "A1:G19");
            List<SubjectSchedule> subjectSchedules = new ArrayList<>();
            for (int i = 1; i < 9; i++) {
                List<String> strings = subjectsScheduleList.get().get(i);
                for (int j = 1; j < strings.size(); j++) {
                    Integer a = j;
                    if (!strings.get(a).equals("")) {
                        subjectSchedules.add(SubjectSchedule.builder()
                                .semester(1)
                                .dayOfWeek(DayOfWeek.of(j))
                                .subjectNumber(i)
                                .subject(subjects.stream().filter(subject -> subject.getName().equals(strings.get(a))).findFirst().get())
                                .build());
                    }
                }
            }
            for (int i = 12; i < 19; i++) {
                List<String> strings = subjectsScheduleList.get().get(i);
                for (int j = 1; j < strings.size(); j++) {
                    Integer a = j;
                    if (!strings.get(a).equals("")) {
                        subjectSchedules.add(SubjectSchedule.builder()
                                .semester(2)
                                .dayOfWeek(DayOfWeek.of(j))
                                .subjectNumber(i)
                                .subject(subjects.stream().filter(subject -> subject.getName().equals(strings.get(a))).findFirst().get())
                                .build());
                    }
                }
            }

            //schedule Year
            Optional<List<List<String>>> schoolYearScheduleStr = sheetListener.getSheetList(journal.get(5), "yearSchedule", "A1:B4");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            SchoolYearSchedule schoolYearSchedule = SchoolYearSchedule.builder()
                    .firstQuarterStart(LocalDate.parse(schoolYearScheduleStr.get().get(0).get(0), formatter))
                    .firstQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get().get(0).get(1), formatter))
                    .secondQuarterStart(LocalDate.parse(schoolYearScheduleStr.get().get(1).get(0), formatter))
                    .secondQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get().get(1).get(1), formatter))
                    .threeQuarterStart(LocalDate.parse(schoolYearScheduleStr.get().get(2).get(0), formatter))
                    .threeQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get().get(2).get(1), formatter))
                    .fourQuarterStart(LocalDate.parse(schoolYearScheduleStr.get().get(3).get(0), formatter))
                    .fourQuarterEnd(LocalDate.parse(schoolYearScheduleStr.get().get(3).get(1), formatter))
                    .build();

            //consolidate statement
            Optional<List<List<String>>> consStat = sheetListener.getSheetList(journal.get(5), "firstQuarterNumbers", "A1:T22");
            List<List<String>> transpose = transpose(consStat.get());



            journalList.add(
                    Journal.builder()
                            .journalId(journal.get(0))
                            .students(studentList)
                            .teachers(teachers1.stream().toList())
                            .subjects(subjects)
                            .classParallel(journal.get(1))
                            .classLetter(journal.get(2))
                            .nameGUO(journal.get(3))
                            .classroomTeacher(teacherRepository.findByTeacherId(journal.get(4)))
                            .schoolYearSchedule(schoolYearSchedule)
                            //  .consolidatedStatement()//todo
                            .subjectSchedules(subjectSchedules)
                            .build());
        }
        journalRepository.setJournals(journalList);
    }

    static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }
}
