package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.repositories.DialogAttributeRepository;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class StudentService {



    private HashMap<String, String> correspondenceOfDates;

    public String getStartCell(Student student, String dayOfMonth) {
        String s = correspondenceOfDates.get(dayOfMonth);
        String line = String.valueOf((Integer.parseInt(student.getNumber()) + 3));
        return s + line;
    }
}
