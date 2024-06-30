package by.faeton.lyceumteacherbot.repositories;

import by.faeton.lyceumteacherbot.model.lyceum.Student;
import by.faeton.lyceumteacherbot.model.lyceum.Teacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentsRepository {
    private List<Student> students;

    void setStudents(List<Student> students) {
        this.students = students;
    }
    public Student findByStudentId(String studentId) {
        return students.stream().filter(student -> student.getStudentId().equals(studentId)).findFirst().get();
    }
}
