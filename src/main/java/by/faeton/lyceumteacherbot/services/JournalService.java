package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.DTO.NumberDateNumberOfSubject;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.DTO.NumberSubject;
import by.faeton.lyceumteacherbot.model.DTO.StudentWithNumberAndNumberOfTask;
import by.faeton.lyceumteacherbot.model.lyceum.Journal;
import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.model.lyceum.SubjectNumber;
import by.faeton.lyceumteacherbot.model.lyceum.Task;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JournalService {
    private final JournalRepository journalRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;

    public List<NumberDateSubject> getNumbers(String studentId, Integer year, LocalDate localDate/*todo*/) {
        List<NumberDateSubject> numberDateSubjects = new ArrayList<>();
        journalRepository.findByStudentIdAndYear(studentId, year).ifPresent(journal -> journal.getSubjects()
                .forEach(subject -> subject.getTasks()
                        .forEach(task -> task.getSubjectNumbers()
                                .forEach(subjectNumber -> {
                                    if (subjectNumber.getStudent().getStudentId().equals(studentId)) {
                                        numberDateSubjects.add(new NumberDateSubject(subjectNumber.getValuee(), task.getDate(), subject.getName()));
                                    }
                                }))));
        return numberDateSubjects;
    }

    public List<NumberSubject> getQuarterNumbers(String studentId, Integer year, LocalDate localDate/*todo*/) {
        List<NumberSubject> numberSubjects = new ArrayList<>();
        journalRepository.findByStudentIdAndYear(studentId, year).ifPresent(journal -> journal.getConsolidatedStatement().getFirstQuarterNumbers()
                .forEach(consolidatedSubject -> consolidatedSubject.getSubjectNumber()
                        .forEach(subjectNumber -> {
                            if (subjectNumber.getStudent().getStudentId().equals(studentId)) {
                                numberSubjects.add(new NumberSubject(subjectNumber.getValuee(), consolidatedSubject.getSubject().getName()));
                            }
                        })));
        return numberSubjects;
    }

    public List<Student> getStudentsFromClass(String classLetter, String classParallel, Integer year) {
        Optional<Journal> byClassLetterAndClassParallel = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, year);
        if (byClassLetterAndClassParallel.isPresent()) {
            return byClassLetterAndClassParallel.get().getStudents();
        } else {
            return new ArrayList<>();
        }
    }

    public List<Task> getTasksOnCurrentDate(LocalDate dateTime, String classLetter, String classParallel, Integer year) {
        Optional<Journal> byClassLetterAndClassParallel = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel,year);
        List<Task> tasks = new ArrayList<>();
        byClassLetterAndClassParallel.ifPresent(journal -> journal.getSubjects()
                .forEach(s -> tasks.addAll(s.getTasks().stream()
                        .filter(task -> task.getDate().equals(dateTime))
                        .toList())));
        return tasks;
    }

    public List<StudentWithNumberAndNumberOfTask> getStudentsAbseentism(LocalDate dateTime, String classLetter, String classParallel, Integer year) {
        List<Task> tasksOnCurrentDate = getTasksOnCurrentDate(dateTime, classLetter, classParallel, year);
        List<StudentWithNumberAndNumberOfTask> studentWithNumberAndNumberOfTasks = new ArrayList<>();
        tasksOnCurrentDate
                .forEach(task -> task.getSubjectNumbers()
                        .forEach(subjectNumber -> {
                            if (typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism().containsKey(subjectNumber.getValuee())) {
                                studentWithNumberAndNumberOfTasks.add(new StudentWithNumberAndNumberOfTask(subjectNumber.getStudent().getUserLastName(),
                                        subjectNumber.getValuee(),
                                        task.getTaskNumber()));
                            }
                        }));
        return studentWithNumberAndNumberOfTasks;
    }

    public boolean writeAbsenteeism(List<NumberDateNumberOfSubject> list, Student student, String classParallel, String classLetter, Integer year) {
        Optional<Journal> byClassLetterAndClassParallel = journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classParallel, year);
        list
                .forEach(numberDateNumberOfSubject -> byClassLetterAndClassParallel.ifPresent(journal -> {
                            journal.getSubjects()
                                    .forEach(subject -> subject.getTasks().stream()
                                            .filter(task -> task.getDate().equals(numberDateNumberOfSubject.getLocalDate()))
                                            .filter(task -> task.getTaskNumber().equals(numberDateNumberOfSubject.getNumberOfSubject()))
                                            .forEach(task -> {
                                                Optional<SubjectNumber> first = task.getSubjectNumbers().stream()
                                                        .filter(subjectNumber -> subjectNumber.getStudent().equals(student))
                                                        .findFirst();
                                                if (first.isPresent()) {
                                                    first.get().setValuee(numberDateNumberOfSubject.getNumber());
                                                } else {
                                                    List<SubjectNumber> subjectNumbers = task.getSubjectNumbers();
                                                    SubjectNumber subjectNumber = new SubjectNumber();
                                                    subjectNumber.setValuee(numberDateNumberOfSubject.getNumber());
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
}