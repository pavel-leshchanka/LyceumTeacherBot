package by.faeton.lyceumteacherbot.utils.addressgenerator;

import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CellAddressGenerator {

    private static final Integer NUMBER_OF_LETTERS = 26;
    private static final Integer SHIFT_TO_LETTER_A = 64;
    private static FieldsNameConfig fieldsNameConfig;

    public CellAddressGenerator(FieldsNameConfig fieldsNameConfig) {
        this.fieldsNameConfig = fieldsNameConfig;
    }

    public static String getNameOfStartCellOfAbsenteeism(Student student, Integer columnNumber) {
        String s = convertNumberColumnToLetter(columnNumber);
        String line = String.valueOf((Integer.parseInt(student.getStudentNumber()) + fieldsNameConfig.numberOfFirstColumnWithAbsenteeism()));
        return s + line;
    }

    public static String convertNumberColumnToLetter(Integer columnNumber) {
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
        return startCell;
    }
}
