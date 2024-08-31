package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.TeacherRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;

@Service
@RequiredArgsConstructor
public class BotService {
    private final JournalRepository journalRepository;
    private final StudentsRepository studentsRepository;
    private final TeacherRepository teacherRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final UserRepository userRepository;

    public void refreshContext() {
        journalRepository.refreshContext();
        studentsRepository.refreshContext();
        teacherRepository.refreshContext();
        typeAndValueOfAbsenteeismRepository.refreshContext();
        userRepository.refreshContext();
    }
}
