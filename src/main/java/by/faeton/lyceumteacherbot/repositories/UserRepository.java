package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SheetListNameConfig sheetListNameConfig;
    private final SheetListener sheetListener;

    private final Set<String> classParallels;
    private final Set<String> classLetters;
    private final Set<String> sex;
    private final Set<String> studentClasses;

    private final List<User> usersList;

    public Optional<User> findByTelegramId(Long telegramId) {
        Optional<User> returnedUser = usersList.stream()
                .filter(user -> user.getTelegramUserId().equals(telegramId))
                .findFirst();
        if (returnedUser.isEmpty()) {
            log.info("User {} not found", telegramId);
            refreshContext();
            returnedUser = usersList.stream()
                    .filter(user -> user.getTelegramUserId().equals(telegramId))
                    .findFirst();
        }
        return returnedUser;
    }

    public List<User> getAllUsers() {
        refreshContext();
        return List.copyOf(usersList);
    }

    private void refreshContext() {
        log.info("Called refresh context method");
        Optional<List<List<String>>> values = sheetListener.getSheetList(sheetListNameConfig.baseIdList());
        classParallels.clear();
        classLetters.clear();
        sex.clear();
        studentClasses.clear();
        usersList.clear();
        if (values.isPresent()) {
            for (List<String> value : values.get()) {
                try {
                    usersList.add(User.builder()
                            .telegramUserId(Long.valueOf(value.get(0)))
                            .classParallel(value.get(1))
                            .classLetter(value.get(2))
                            .fieldOfSheetWithUser(value.get(3))
                            .userLastName(value.get(4))
                            .userFirstName(value.get(5))
                            .userFatherName(value.get(6))
                            .sex(value.get(7))
                            .userLevel(UserLevel.valueOf(value.get(8)))
                            .build());
                    if (!value.get(1).equals("")) {
                        classParallels.add(value.get(1));
                    }
                    if (!value.get(2).equals("")) {
                        classLetters.add(value.get(2));
                    }
                    if (!value.get(7).equals("")) {
                        sex.add(value.get(7));
                    }
                    if (UserLevel.valueOf(value.get(8)).equals(UserLevel.ADMIN)) {
                        studentClasses.add(value.get(1) + value.get(2));
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public Set<String> getClassParallels() {
        return Set.copyOf(classParallels);
    }

    public Set<String> getClassLetters() {
        return Set.copyOf(classLetters);
    }

    public Set<String> getSex() {
        return Set.copyOf(sex);
    }

    public Set<String> getStudentClasses() {
        return Set.copyOf(studentClasses);
    }
}