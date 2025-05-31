package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.exceptions.ResourceNotFoundException;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    public static final String JOURNAL_NOT_FOUND = "Журнал не найден";
    public static final String STUDENT_NOT_FOUND = "Ученик не найден";
    private final JournalRepository journalRepository;
    private final StudentsRepository studentsRepository;

    public String getClassLetter(String studentId, Integer year) {
        return journalRepository.findByStudentIdAndYear(studentId, year)
            .orElseThrow(() -> new ResourceNotFoundException(JOURNAL_NOT_FOUND))
            .getClassLetter();
    }

    public String getClassParallel(String studentId, Integer year) {
        return journalRepository.findByStudentIdAndYear(studentId, year)
            .orElseThrow(() -> new ResourceNotFoundException(JOURNAL_NOT_FOUND))
            .getClassParallel();
    }

    public Student findByStudentId(String studentId) {
        return studentsRepository.findByStudentId(studentId)
            .orElseThrow(() -> new ResourceNotFoundException(STUDENT_NOT_FOUND));
    }
}
