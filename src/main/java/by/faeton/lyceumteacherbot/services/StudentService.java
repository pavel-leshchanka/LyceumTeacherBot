package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.Student;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@NoArgsConstructor
public class StudentService {



    private HashMap<String, String> correspondenceOfDates;

    public String getStartCell(Student student, Integer dayOfMonth, Integer shift) {
        int i = dayOfMonth * 8 - 7 + 2 + shift;
        String s = "";
        while (i>26){
            int i1 = i / 26;
            int ost=i-i1*26;
            char f = (char) (ost+64);
            s=f+s;
            i=i1;
        }
        s=(char) (i+64)+s;

        String line = String.valueOf((Integer.parseInt(student.getNumber()) + 3));
        return s + line;//todo
    }

    public String getStartCell(Student student, Integer dayOfMonth) {
        return getStartCell(student, dayOfMonth, 0);
    }
}
