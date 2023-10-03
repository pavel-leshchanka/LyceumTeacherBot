package by.faeton.lyceumteacherbot.utils;


import by.faeton.lyceumteacherbot.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.SneakyThrows;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Serializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @SneakyThrows
    public List loadListState(String path) {
        CSVReader reader = new CSVReader(new FileReader(path));
        List<User> users = new ArrayList<>();
        List<String[]> records = reader.readAll();
        Iterator<String[]> iterator = records.iterator();
        while (iterator.hasNext()) {
            String[] record = iterator.next();
            String[] split = record[0].split(";");
            User user = new User();
            user.setId(Integer.parseInt(split[0]));
            user.setList(split[1]);
            user.setField(split[2]);
            users.add(user);
        }
        return users;
    }
}
