package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.TeacherRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.security.TelegramUserRepository;
import by.faeton.lyceumteacherbot.utils.NumbersFromMyTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotService {
    private final JournalRepository journalRepository;
    private final StudentsRepository studentsRepository;
    private final TeacherRepository teacherRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final TelegramUserRepository userRepository;
    private final NumbersFromMyTable numbersFromMyTable;

    public void refreshContext() {
        userRepository.refreshContext();
        studentsRepository.refreshContext();
        teacherRepository.refreshContext();
        typeAndValueOfAbsenteeismRepository.refreshContext();
        journalRepository.refreshContext();
        numbersFromMyTable.init();
    }
}
