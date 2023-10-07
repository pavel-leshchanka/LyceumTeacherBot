package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.BotConfig;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepository {
    @Autowired
    private final BotConfig botConfig;
    private final SheetListener sheetListener;

    private final List<User> usersList = new ArrayList<>();

    public Optional<User> get(String id) {
        Optional<User> returnedUser = usersList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
        if (returnedUser.isEmpty()){
            setUp();
        }
        return returnedUser;
    }

    @SneakyThrows
    @PostConstruct
    public void setUp() {
        String baseId = sheetListener.getString(botConfig.getBaseIdList());
        List<User> list = new ArrayList<>();
        HashMap<String, Object> result = new ObjectMapper().readValue(baseId, HashMap.class);
        ArrayList<Object> values = (ArrayList<Object>) result.get("values");
        if (values != null) {
            for (Object value : values) {
                ArrayList<String> sheetLine = (ArrayList<String>) value;
                User user = new User();
                user.setId(sheetLine.get(0));
                user.setList(sheetLine.get(1));
                user.setField(sheetLine.get(2));
                list.add(user);
            }
        }
        usersList.clear();
        usersList.addAll(list);
    }

}
