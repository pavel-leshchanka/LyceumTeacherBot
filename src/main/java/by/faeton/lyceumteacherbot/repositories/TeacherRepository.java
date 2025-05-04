package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.Teacher;
import by.faeton.lyceumteacherbot.utils.SheetListener;
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

    private final SheetListener sheetListener;
    private final SheetConfig sheetConfig;
    private final SheetListNameConfig sheetListNameConfig;
    private final FieldsNameConfig fieldsNameConfig;
    private final List<Teacher> teachers;

    public Optional<Teacher> findByTeacherId(String teacherId) {
        return teachers.stream().filter(teacher -> teacher.getTeacherId().equals(teacherId)).findFirst();
    }

    @PostConstruct
    public void refreshContext() {
        teachers.clear();
        teachers.addAll(sheetListener.getSheetList(sheetConfig.sheetId(), sheetListNameConfig.allTeachers(), fieldsNameConfig.allTeachers()).orElseThrow().stream()
            .map(strings -> Teacher.builder()
                .teacherId(strings.get(0))
                .name(strings.get(1))
                .build())
            .collect(Collectors.toCollection(ArrayList::new)));
    }
}
