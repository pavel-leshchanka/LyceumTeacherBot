package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
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
public class TeacherRepository {
    private static final String MASTER_SHEET_ID = "1m50PxnhIYP-5rXjrXVYfJDw6E4NmEqgb-pGtY9gbG5c";

    private final List<Teacher> teachers;
    private final SheetListener sheetListener;

    public Optional<Teacher> findByTeacherId(String teacherId) {
        return teachers.stream().filter(teacher -> teacher.getTeacherId().equals(teacherId)).findFirst();
    }

    @PostConstruct
    private void setUp() {
        teachers.clear();
        teachers.addAll(sheetListener.getSheetList(MASTER_SHEET_ID, "teachers", "A2:F100").orElseThrow().stream()
                .map(strings -> Teacher.builder()
                        .teacherId(strings.get(0))
                        .name(strings.get(1))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new)));
    }
}
