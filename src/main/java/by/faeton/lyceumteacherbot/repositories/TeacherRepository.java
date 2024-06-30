package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TeacherRepository {
    private List<Teacher> teachers;

    void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public Teacher findByTeacherId(String teacherId) {
        return teachers.stream().filter(teacher -> teacher.getTeacherId().equals(teacherId)).findFirst().get();
    }
}
