package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final JournalRepository journalRepository;

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
}
