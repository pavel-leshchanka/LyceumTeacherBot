package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.Student;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentsRepository {
    private static final String MASTER_SHEET_ID = "1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c";
    private final List<Student> students;
    private final SheetListener sheetListener;

    public Optional<Student> findByStudentId(String studentId) {
        return students.stream()
                .filter(student -> student.getStudentId().equals(studentId))
                .findFirst();
    }

    @PostConstruct
    private void setUp() {
        students.clear();
        students.addAll(sheetListener.getSheetList(MASTER_SHEET_ID, "students", "A2:F100").orElseThrow().stream()
                .map(strings -> Student.builder()
                        .studentId(strings.get(0))
                        .userFirstName(strings.get(1))
                        .userLastName(strings.get(2))
                        .userFatherName(strings.get(3))
                        .sex(strings.get(4))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new)));
    }
}
