package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentsRepository {

    private final SheetListener sheetListener;
    private final SheetListNameConfig sheetListNameConfig;
    private final SheetConfig sheetConfig;
    private final FieldsNameConfig fieldsNameConfig;
    private final List<Student> students;
    private final Set<String> sex;

    public Optional<Student> findByStudentId(String studentId) {
        return students.stream()
                .filter(student -> student.getStudentId().equals(studentId))
                .findFirst();
    }

    public Set<String> getAllStudentsSex() {
        return Set.copyOf(sex);
    }

    @PostConstruct
    public void refreshContext() {
        students.clear();
        students.addAll(sheetListener.getSheetList(sheetConfig.sheetId(), sheetListNameConfig.allStudents(), fieldsNameConfig.allStudents()).orElseThrow().stream()
                .map(strings -> Student.builder()
                        .studentId(strings.get(0))
                        .userLastName(strings.get(1))
                        .userFirstName(strings.get(2))
                        .userFatherName(strings.get(3))
                        .sex(strings.get(4))
                        .build())
                .collect(Collectors.toCollection(ArrayList::new)));
        sex.clear();
        students.forEach(student -> {
            String studentSex = student.getSex();
            if (!studentSex.isEmpty()) {
                sex.add(studentSex);
            }
        });
    }
}
