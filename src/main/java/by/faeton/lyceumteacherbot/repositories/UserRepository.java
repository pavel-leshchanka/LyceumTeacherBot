package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.utils.Serializer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final List<User> usersList = new ArrayList<>();

    public Optional<User> get(String id) {
        Optional<User> returnedUser = usersList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
        return returnedUser;
    }

    @Autowired
    public void setUp() {
        List list = new Serializer().loadListState("baseId.csv");
        usersList.addAll(list);
    }

}
