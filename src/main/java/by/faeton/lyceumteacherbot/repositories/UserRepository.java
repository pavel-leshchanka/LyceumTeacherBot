package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.DTO.UserRegisterDTO;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SheetListNameConfig sheetListNameConfig;
    private final SheetListener sheetListener;
    private final SheetConfig sheetConfig;


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

    public List<User> findBySubjectOfEducationId(String subjectOfEducationId) {
        return usersList.stream()
                .filter(user -> user.getSubjectOfEducationId().equals(subjectOfEducationId))
                .toList();
    }

    public void registerNewUser(UserRegisterDTO user) {
        sheetListener.writeNewUser(List.of(List.of(
                user.getUserLastName(),
                user.getUserFirstName(),
                user.getUserFatherName(),
                user.getTelegramUserId(),
                user.getSex(),
                user.getClassName(),
                user.getUserLevel())));
    }

    public List<User> getAllUsers() {
        refreshContext();
        return List.copyOf(usersList);
    }

    public void refreshContext() {
        log.info("Called refresh context method");
        Optional<List<List<String>>> values = sheetListener.getSheetList(sheetConfig.sheetId(), sheetListNameConfig.baseIdList());
        usersList.clear();
        values.ifPresent(s -> s.forEach(value -> {
                    try {
                        usersList.add(User.builder()
                                .telegramUserId(Long.valueOf(value.get(0)))
                                .userLastName(value.get(4))
                                .userFirstName(value.get(5))
                                .userFatherName(value.get(6))
                                .sex(value.get(7))
                                .userLevel(UserLevel.valueOf(value.get(8)))
                                .subjectOfEducationId(value.get(9))
                                .build());
                    } catch (Exception e) {

                    }
                })
        );
    }
}