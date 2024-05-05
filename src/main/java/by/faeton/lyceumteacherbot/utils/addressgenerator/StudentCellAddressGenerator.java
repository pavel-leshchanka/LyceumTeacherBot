package by.faeton.lyceumteacherbot.utils.addressgenerator;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentCellAddressGenerator {

    private static final Integer NUMBER_OF_LETTERS = 26;
    private static final Integer SHIFT_TO_LETTER_A = 64;
    private final FieldsNameConfig fieldsNameConfig;

    public String getNameOfStartCellOfAbsenteeism(Student student, Integer columnNumber) {
        int number = columnNumber;
        String startCell = "";
        while (number > NUMBER_OF_LETTERS) {
            int letterNumber = number % NUMBER_OF_LETTERS;
            if (letterNumber == 0) {
                letterNumber = NUMBER_OF_LETTERS;
                number -= 1;
            }
            char letter = (char) (letterNumber + SHIFT_TO_LETTER_A);
            startCell = letter + startCell;
            number /= NUMBER_OF_LETTERS;
        }
        startCell = (char) (number + SHIFT_TO_LETTER_A) + startCell;
        String line = String.valueOf((Integer.parseInt(student.getStudentNumber()) + fieldsNameConfig.numberOfFirstColumnWithAbsenteeism()));
        return startCell + line;
    }
}
