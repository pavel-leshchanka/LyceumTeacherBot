package by.faeton.lyceumteacherbot.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CellAddressGenerator {
    private static final Integer NUMBER_OF_LETTERS = 26;
    private static final Integer SHIFT_TO_LETTER_A = 64;

    public static String convertNumberColumnToLetter(Integer columnNumber) {
        int number = columnNumber;
        StringBuilder startCell = new StringBuilder();
        while (number > NUMBER_OF_LETTERS) {
            int letterNumber = number % NUMBER_OF_LETTERS;
            if (letterNumber == 0) {
                letterNumber = NUMBER_OF_LETTERS;
                number -= 1;
            }
            char letter = (char) (letterNumber + SHIFT_TO_LETTER_A);
            startCell.insert(0, letter);
            number /= NUMBER_OF_LETTERS;
        }
        startCell.insert(0, (char) (number + SHIFT_TO_LETTER_A));
        return startCell.toString();
    }
}
