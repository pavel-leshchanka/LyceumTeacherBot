package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final JournalRepository journalRepository;
    private final StudentsRepository studentsRepository;

    public String getClassLetter(String studentId, Integer year) {
        return journalRepository.findByStudentIdAndYear(studentId, year)
            .orElseThrow()
            .getClassLetter();
    }

    public String getClassParallel(String studentId, Integer year) {
        return journalRepository.findByStudentIdAndYear(studentId, year)
            .orElseThrow()
            .getClassParallel();
    }

    public Student findByStudentId(String studentId) {
        return studentsRepository.findByStudentId(studentId)
            .orElseThrow();
    }
}
