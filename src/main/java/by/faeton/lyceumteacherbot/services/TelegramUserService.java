package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.controllers.handlers.dto.RegisterDTO;
import by.faeton.lyceumteacherbot.security.TelegramUser;
import by.faeton.lyceumteacherbot.security.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramUserService {
    private final TelegramUserRepository userRepository;

    public void createNewUser(RegisterDTO dtoFromCallback, Long chatId) {
        userRepository.registerNewUser(dtoFromCallback, chatId);
    }

    public TelegramUser findByTelegramId(Long chatId) {
        return userRepository.findByTelegramId(chatId)
            .orElseThrow();
    }

    public List<TelegramUser> findBySubjectOfEducationId(String studentId) {
        return userRepository.findBySubjectOfEducationId(studentId);
    }
}
