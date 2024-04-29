package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    @Getter
    private final Set<String> classParallels;
    @Getter
    private final Set<String> classLetters;
    @Getter
    private final Set<String> sex;

    @PostConstruct
    private void setUp() {
        List<User> allUsers = userRepository.getAllUsers();
        allUsers.forEach(user -> {
            classParallels.add(user.getClassParallel());
            classLetters.add(user.getClassLetter());
            sex.add(user.getSex());
        });
    }
}
