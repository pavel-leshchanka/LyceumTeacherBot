package by.faeton.lyceumteacherbot.security;

import by.faeton.lyceumteacherbot.config.SheetConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.RegisterDTO;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TelegramUserRepository {
    private final SheetListNameConfig sheetListNameConfig;
    private final SheetListener sheetListener;
    private final SheetConfig sheetConfig;
    private final List<TelegramUser> usersList;

    public Optional<TelegramUser> findByTelegramId(Long telegramId) {
        Optional<TelegramUser> returnedUser = usersList.stream()
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

    public List<TelegramUser> findBySubjectOfEducationId(String subjectOfEducationId) {
        return usersList.stream()
            .filter(user -> user.getSubjectOfEducationId().equals(subjectOfEducationId))
            .toList();
    }

    public void registerNewUser(RegisterDTO user, Long id) {
        sheetListener.writeNewUser(List.of(
                List.of(
                    user.getUserLastName(),
                    user.getUserFirstName(),
                    user.getUserFatherName(),
                    id,
                    user.getSex(),
                    user.getClassName(),
                    user.getUserLevel()
                )
            )
        );
    }

    public void refreshContext() {
        log.info("Called refresh context method");
        usersList.clear();
        sheetListener.getSheetList(sheetConfig.sheetId(), sheetListNameConfig.baseIdList())
            .ifPresent(line -> line.forEach(value -> {
                    try {
                        usersList.add(TelegramUser.builder()
                            .telegramUserId(Long.valueOf(value.get(0)))
                            .userLastName(value.get(4))
                            .userFirstName(value.get(5))
                            .userFatherName(value.get(6))
                            .sex(value.get(7))
                            .userLevel(UserLevel.valueOf(value.get(8)))
                            .subjectOfEducationId(value.get(9))
                            .build());
                    } catch (Exception e) {
                        log.error("Create user exception", e);
                    }
                })
            );
    }
}
