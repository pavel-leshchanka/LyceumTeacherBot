package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.SendMessagesDTO;
import by.faeton.lyceumteacherbot.exceptions.ResourceNotFoundException;
import by.faeton.lyceumteacherbot.model.ConsolidatedSubject;
import by.faeton.lyceumteacherbot.model.Journal;
import by.faeton.lyceumteacherbot.model.SchoolYearSchedule;
import by.faeton.lyceumteacherbot.model.Statement;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.Subject;
import by.faeton.lyceumteacherbot.model.SubjectNumber;
import by.faeton.lyceumteacherbot.model.SubjectSchedule;
import by.faeton.lyceumteacherbot.model.Task;
import by.faeton.lyceumteacherbot.model.Teacher;
import by.faeton.lyceumteacherbot.model.dto.NumberDateNumberOfSubject;
import by.faeton.lyceumteacherbot.model.dto.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.dto.NumberSubject;
import by.faeton.lyceumteacherbot.model.dto.StudentWithNumberAndNumberOfTask;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static by.faeton.lyceumteacherbot.controllers.handlers.SendMessagesHandler.ALL_CALLBACK;
import static by.faeton.lyceumteacherbot.services.StudentService.JOURNAL_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class JournalService {
    private final JournalRepository journalRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final SchoolConfig schoolConfig;
    private final StudentsRepository studentsRepository;

    public Set<String> getClassParallels() {
        return journalRepository.getClassParallels();
    }

    public Set<String> getClassLetters() {
        return journalRepository.getClassLetters();
    }

    public List<Subject> getSubjects(String studentId, Integer integer) {
        Journal journal = journalRepository.findByStudentIdAndYear(studentId, integer)
            .orElseThrow(() -> new ResourceNotFoundException(JOURNAL_NOT_FOUND));
        return journal.getSubjects();
    }

    public List<Student> findUsersByParameters(SendMessagesDTO dialogAttribute) {
        String classParallels = dialogAttribute.getClassParallels();
        String classLetters = dialogAttribute.getClassLetters();
        String sex = dialogAttribute.getSex();
        return journalRepository.findAllByYear(schoolConfig.currentAcademicYear()).stream()
            .filter(u -> {
                if (classParallels.equals(ALL_CALLBACK)) {
                    return true;
                } else {
                    return u.getClassParallel().equals(classParallels);
                }
            })
            .filter(u -> {
                if (classLetters.equals(ALL_CALLBACK)) {
                    return true;
                } else {
                    return u.getClassLetter().equals(classLetters);
                }
            })
            .flatMap(s -> s.getStudents().stream())
            .filter(u -> {
                if (sex.equals(ALL_CALLBACK)) {
                    return true;
                } else {
                    return u.getSex().equals(sex);
                }
            })
            .toList();
    }


    public List<NumberDateSubject> getNumbers(String studentId, Integer year, Statement statement, String subjectId) {
        List<NumberDateSubject> numberDateSubjects = new ArrayList<>();
        journalRepository.findByStudentIdAndYear(studentId, year).ifPresent(journal -> journal.getSubjects().stream()
            .filter(subject -> subject.getId().equals(Long.parseLong(subjectId)))
            .forEach(subject -> subject.getTasks()
                .forEach(task -> task.getSubjectNumbers()
                    .forEach(subjectNumber -> {
                        if (subjectNumber.getStudent().getStudentId().equals(studentId)) {
                            SchoolYearSchedule schoolYearSchedule = journal.getSchoolYearSchedule();
                            LocalDate start = switch (statement) {
                                case FIRST_QUARTER -> schoolYearSchedule.getFirstQuarterStart();
                                case SECOND_QUARTER -> schoolYearSchedule.getSecondQuarterStart();
                                case THREE_QUARTER -> schoolYearSchedule.getThreeQuarterStart();
                                case FOUR_QUARTER -> schoolYearSchedule.getFourQuarterStart();
                                default -> throw new IllegalStateException("Unexpected text: " + statement);
                            };
                            LocalDate end = switch (statement) {
                                case FIRST_QUARTER -> schoolYearSchedule.getFirstQuarterEnd();
                                case SECOND_QUARTER -> schoolYearSchedule.getSecondQuarterEnd();
                                case THREE_QUARTER -> schoolYearSchedule.getThreeQuarterEnd();
                                case FOUR_QUARTER -> schoolYearSchedule.getFourQuarterEnd();
                                default -> throw new IllegalStateException("Unexpected text: " + statement);
                            };
                            if ((task.getDate().isAfter(start) && task.getDate().isBefore(end)) || task.getDate().equals(start) || task.getDate().equals(end)) {
                                numberDateSubjects.add(new NumberDateSubject(subjectNumber.getValueOfTask(), task.getDate(), task.getThemeName(), subject.getName()));
                            }
                        }
                    })
                )
            )
        );
        return numberDateSubjects;
    }

    public List<NumberSubject> getStatementNumbers(String studentId, Integer year, Statement statement) {
        List<NumberSubject> numberSubjects = new ArrayList<>();
        journalRepository.findByStudentIdAndYear(studentId, year).ifPresent(journal -> {
                List<ConsolidatedSubject> numbers = switch (statement) {
                    case FIRST_QUARTER:
                        yield journal.getConsolidatedStatement().getFirstQuarterNumbers();
                    case SECOND_QUARTER:
                        yield journal.getConsolidatedStatement().getSecondQuarterNumbers();
                    case THREE_QUARTER:
                        yield journal.getConsolidatedStatement().getThreeQuarterNumbers();
                    case FOUR_QUARTER:
                        yield journal.getConsolidatedStatement().getFourQuarterNumbers();
                    case YEAR:
                        yield journal.getConsolidatedStatement().getYearNumbers();
                    case EXAM:
                        yield journal.getConsolidatedStatement().getExamNumbers();
                    case FINAL:
                        yield journal.getConsolidatedStatement().getFinalNumbers();
                };
                numbers.forEach(consolidatedSubject -> consolidatedSubject.getSubjectNumber()
                    .forEach(subjectNumber -> {
                        if (subjectNumber.getStudent().getStudentId().equals(studentId)) {
                            numberSubjects.add(new NumberSubject(subjectNumber.getValueOfTask(), consolidatedSubject.getSubject().getName()));
                        }
                    })
                );
            }
        );
        return numberSubjects;
    }

    public List<Student> getStudentsFromClass(String classLetter, String classParallel, Integer year) {
        return journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, year).stream()
            .flatMap(journal -> journal.getStudents().stream())
            .toList();
    }

    public List<Task> getTasksOnCurrentDate(LocalDate dateTime, String classLetter, String classParallel, Integer year) {
        return journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, year).stream()
            .flatMap(journal -> journal.getSubjects().stream()
                .flatMap(s -> s.getTasks().stream()
                    .filter(task -> task.getDate().equals(dateTime))))
            .toList();
    }

    public List<StudentWithNumberAndNumberOfTask> getStudentsAbsenteeism(LocalDate dateTime, String classLetter, String classParallel, Integer year) {
        List<Task> tasksOnCurrentDate = getTasksOnCurrentDate(dateTime, classLetter, classParallel, year);
        List<StudentWithNumberAndNumberOfTask> studentWithNumberAndNumberOfTasks = new ArrayList<>();
        tasksOnCurrentDate
            .forEach(task -> task.getSubjectNumbers()
                .forEach(subjectNumber -> {
                    if (typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism().containsKey(subjectNumber.getValueOfTask())) {
                        studentWithNumberAndNumberOfTasks.add(new StudentWithNumberAndNumberOfTask(subjectNumber.getStudent().getUserLastName() + " " + subjectNumber.getStudent().getUserFirstName(),
                            subjectNumber.getValueOfTask(),
                            task.getTaskNumber()));
                    }
                })
            );
        return studentWithNumberAndNumberOfTasks;
    }

    public boolean writeAbsenteeism(List<NumberDateNumberOfSubject> list, Student student, String classParallel, String classLetter, Integer year) {
        Optional<Journal> byClassLetterAndClassParallel = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, year);
        list.forEach(numberDateNumberOfSubject -> byClassLetterAndClassParallel.ifPresent(journal -> {
                journal.getSubjects()
                    .forEach(subject -> subject.getTasks().stream()
                        .filter(task -> task.getDate().equals(numberDateNumberOfSubject.getLocalDate()))
                        .filter(task -> task.getTaskNumber().equals(numberDateNumberOfSubject.getNumberOfSubject()))
                        .forEach(task -> {
                            Optional<SubjectNumber> first = task.getSubjectNumbers().stream()
                                .filter(subjectNumber -> subjectNumber.getStudent().equals(student))
                                .findFirst();
                            if (first.isPresent()) {
                                first.get().setValueOfTask(numberDateNumberOfSubject.getNumber());
                            } else {
                                List<SubjectNumber> subjectNumbers = task.getSubjectNumbers();
                                SubjectNumber subjectNumber = new SubjectNumber();
                                subjectNumber.setValueOfTask(numberDateNumberOfSubject.getNumber());
                                subjectNumber.setStudent(student);
                                subjectNumbers.add(subjectNumber);
                            }
                        })
                    );
                journalRepository.save(journal);
            }
        ));
        return true;
    }

    public Set<String> getAllStudentsSex() {
        return studentsRepository.getAllStudentsSex();
    }

    public Journal findByClassLetterAndClassParallelAndYear(String classLetter, String classParallel, Integer integer) {
        return journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, integer)
            .orElseThrow(() -> new ResourceNotFoundException(JOURNAL_NOT_FOUND));
    }

    public Teacher findClassTeacher(String classLetter, String classParallel, Integer integer) {
        return findByClassLetterAndClassParallelAndYear(classLetter, classParallel, integer).getClassroomTeacher();
    }

    public Map<String, String> getAllTypeAndValueOfAbsenteeism() {
        return typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism();
    }

    public String getValueOfAbsenteeism(String typeOfAbsenteeism) {
        return typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(typeOfAbsenteeism);
    }

    public List<SubjectSchedule> getSchedule(String classParallel, String classLetter, Integer integer) {
        return journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, integer)
            .orElseThrow(() -> new ResourceNotFoundException(JOURNAL_NOT_FOUND))
            .getSubjectSchedules();
    }
}