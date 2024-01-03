package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final BotConfig botConfig;
    private final SheetListener sheetListener;

    private final List<User> usersList = new ArrayList<>();

    public Optional<User> get(String id) {
        Optional<User> returnedUser = usersList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
        if (returnedUser.isEmpty()) {
            setUp();
        }
        return returnedUser;
    }

    public List<User> getAll() {
        return usersList;
    }

    @SneakyThrows
    @PostConstruct
    public void setUp() {
        Optional<ArrayList<ArrayList<String>>> values = sheetListener.getSheetList(botConfig.getBaseIdList());
        List<User> list = new ArrayList<>();
        if (values.isPresent()) {
            for (ArrayList<String> value : values.get()) {
                User user = new User();
                user.setId(value.get(0));
                user.setList(value.get(1));
                user.setField(value.get(2));
                list.add(user);
            }
        }
        usersList.clear();
        usersList.addAll(list);
    }


}
