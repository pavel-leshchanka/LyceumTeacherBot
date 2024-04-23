package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SheetListNameConfig sheetListNameConfig;
    private final SheetListener sheetListener;

    private final List<User> usersList = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    public Optional<User> findById(Long id) {
        Optional<User> returnedUser = usersList.stream()
                .filter(user -> user.getTelegramUserId().equals(id))
                .findFirst();
        if (returnedUser.isEmpty()) {
            log.info("User " + id + " not found");
            refreshContext();
            returnedUser = usersList.stream()
                    .filter(user -> user.getTelegramUserId().equals(id))
                    .findFirst();
        }
        return returnedUser;
    }

    public List<User> getAllUsers() {
        return usersList;
    }

    public void refreshContext() {
        log.info("Called refresh context method");
        Optional<List<List<String>>> values = sheetListener.getSheetList(sheetListNameConfig.baseIdList());
        List<User> list = new ArrayList<>();
        if (values.isPresent()) {
            for (List<String> value : values.get()) {
                try {
                    list.add(User.builder()
                            .telegramUserId(Long.valueOf(value.get(0)))
                            .listOfGoogleSheet(value.get(1))
                            .fieldOfSheetWithUser(value.get(2))
                            .userName(value.get(3))
                            .userLevel(UserLevel.valueOf(value.get(4)))
                            .build());
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        usersList.clear();
        usersList.addAll(list);
    }
}