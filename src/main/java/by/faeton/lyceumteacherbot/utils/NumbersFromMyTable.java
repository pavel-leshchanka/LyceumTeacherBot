package by.faeton.lyceumteacherbot.utils;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.SubjectNumber;
import by.faeton.lyceumteacherbot.model.Task;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NumbersFromMyTable {
    private final JournalRepository journalRepository;
    private final SheetListener sheetListener;
    private final SchoolConfig schoolConfig;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @PostConstruct
    public void init() {
        extracted("10fm", "ФМ", "10");
        extracted("10i", "ИНЖ", "10");
        extracted("11ft1", "ФТ1", "11");
        extracted("11ft2", "ФТ2", "11");
        extracted("11m1", "М1", "11");
        extracted("11m2", "М2", "11");
        extracted("11l", "Л", "11");
        extracted("11e", "Э", "11");
        extracted("11j", "Ю", "11");
    }

    private void extracted(String sheetListName, String classLetter, String classNumber) {
        journalRepository.findByClassLetterAndClassParallelAndYear(classLetter, classNumber, schoolConfig.currentAcademicYear()).ifPresentOrElse(journal ->
                sheetListener.getSheetList("1WKkaIwv1iAG7627OLXU_8CuZ83VdyuUxQr6hLqalJFY", sheetListName, "A1:HD35").ifPresent(listList -> {
                    for (int i = 11; i < 201; i++) {//column
                        String stringDay = listList.get(2).get(i);
                        if (!stringDay.isEmpty()) {
                            for (int j = 4; j < 35; j++) {//row
                                LocalDate localDate = LocalDate.parse(stringDay, formatter);
                                //поиск ученика
                                String studentId = listList.get(j).get(1);
                                String numberPerTaskByStudent = listList.get(j).get(i);
                                String countOfTaskPerDay = listList.get(1).get(i);
                                String typeOfWork = listList.get(3).get(i);
                                journal.getStudents().stream()
                                    .filter(student -> student.getStudentId().equals(studentId))
                                    .findFirst()
                                    .ifPresent(student -> {
                                        //поиск предмета
                                        journal.getSubjects().stream()
                                            .filter(subject -> subject.getId().equals(13L))
                                            .findFirst()
                                            .ifPresent(phis -> {
                                                //список уроков на конкретный день
                                                List<Task> list = phis.getTasks().stream()
                                                    .filter(sa -> sa.getDate().equals(localDate))
                                                    .sorted(Comparator.comparing(Task::getTaskNumber))
                                                    .toList();
                                                //если урок в день первый
                                                if (!numberPerTaskByStudent.isEmpty()) {
                                                    if (countOfTaskPerDay.equals("1")) {
                                                        Task task = list.get(0);
                                                        task.setThemeName(typeOfWork);
                                                        task.getSubjectNumbers().stream()
                                                            .filter(t -> t.getStudent().equals(student))
                                                            .findAny()
                                                            .ifPresentOrElse(any -> any.setValueOfTask(numberPerTaskByStudent),
                                                                () -> {
                                                                    List<SubjectNumber> subjectNumbers = task.getSubjectNumbers();
                                                                    subjectNumbers.add(SubjectNumber.builder()
                                                                        .student(student)
                                                                        .valueOfTask(numberPerTaskByStudent)
                                                                        .build());
                                                                });
                                                    } else {//если урок второй
                                                        Task task = list.get(1);
                                                        task.setThemeName(typeOfWork);
                                                        task.getSubjectNumbers().stream()
                                                            .filter(t -> t.getStudent().equals(student))
                                                            .findAny()
                                                            .ifPresentOrElse(any -> any.setValueOfTask(numberPerTaskByStudent),
                                                                () -> {
                                                                    List<SubjectNumber> subjectNumbers = task.getSubjectNumbers();
                                                                    subjectNumbers.add(SubjectNumber.builder()
                                                                        .student(student)
                                                                        .valueOfTask(numberPerTaskByStudent)
                                                                        .build());
                                                                });
                                                    }
                                                }
                                            });
                                    });
                            }
                        }
                    }
                }),
            () -> System.out.println("error"));
    }
}
