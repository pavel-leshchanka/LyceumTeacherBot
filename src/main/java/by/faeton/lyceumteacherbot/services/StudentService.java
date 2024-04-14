package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Service
@RequiredArgsConstructor
public class StudentService {

    private static final Integer NUMBER_OF_LETTERS = 26;
    private static final Integer SHIFT_TO_LETTER_A = 64;
    private final FieldsNameConfig fieldsNameConfig;


    public String getNameStartCellOfAbsenteeism(Student student, Integer columnNumber) {
        int number = columnNumber;
        String startCell = "";

        while (number > NUMBER_OF_LETTERS) {
            int letterNumber = number % NUMBER_OF_LETTERS;
            char letter = (char) (letterNumber + SHIFT_TO_LETTER_A);
            startCell = letter + startCell;
            number /= NUMBER_OF_LETTERS;
        }
        startCell = (char) (number + SHIFT_TO_LETTER_A) + startCell;

        String line = String.valueOf((Integer.parseInt(student.getStudentNumber()) + fieldsNameConfig.numberOfFirstColumnWithAbsenteeism()));
        return startCell + line;
    }
}
