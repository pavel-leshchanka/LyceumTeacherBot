package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.lyceum.Journal;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final JournalRepository journalRepository;

    public String getClassLetter(String studentId, Integer year) {
        Optional<Journal> byStudentIdAndYear = journalRepository.findByStudentIdAndYear(studentId, year);
        Journal journal = byStudentIdAndYear.orElseThrow();
        return journal.getClassLetter();
    }

    public String getClassParallel(String studentId, Integer year) {
        Optional<Journal> byStudentIdAndYear = journalRepository.findByStudentIdAndYear(studentId, year);
        Journal journal = byStudentIdAndYear.orElseThrow();
        return journal.getClassParallel();
    }
}
