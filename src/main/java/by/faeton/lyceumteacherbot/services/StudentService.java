package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.Student;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Service
@NoArgsConstructor
public class StudentService {

    private static final Integer NUMBER_OF_LETTERS = 26;
    private static final Integer SHIFT_TO_LETTER_A = 64;
    private static final Integer SHIFT_TO_FIRST_COLUMN = 3; //todo hardcode is not well


    public String getStartCell(Student student, Integer columnNumber) {
        int number = columnNumber;
        String startCell = "";

        while (number > NUMBER_OF_LETTERS) {
            int letterNumber = number % NUMBER_OF_LETTERS;
            char letter = (char) (letterNumber + SHIFT_TO_LETTER_A);
            startCell = letter + startCell;
            number /= NUMBER_OF_LETTERS;
        }
        startCell = (char) (number + SHIFT_TO_LETTER_A) + startCell;

        String line = String.valueOf((Integer.parseInt(student.getNumber()) + SHIFT_TO_FIRST_COLUMN));
        return startCell + line;
    }
}
