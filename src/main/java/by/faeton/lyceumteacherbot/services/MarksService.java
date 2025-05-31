package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.MarksDTO;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.QuarterMarksDTO;
import by.faeton.lyceumteacherbot.model.Statement;
import by.faeton.lyceumteacherbot.model.dto.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.dto.NumberSubject;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.security.TelegramUser;
import by.faeton.lyceumteacherbot.utils.DefaultMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalDouble;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;

@Service
@RequiredArgsConstructor
public class MarksService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;

    public String arrivedMarks(TelegramUser user, MarksDTO dtoFromCallback) {
        String quarter = dtoFromCallback.getQuarter();
        String subjectId = dtoFromCallback.getSubject();
        Statement statement = Statement.valueOf(quarter);
        List<NumberDateSubject> numbers = journalService.getNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement, subjectId);
        return linesToString(numbers);
    }

    private String linesToString(List<NumberDateSubject> numberDateSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberDateSubjects.stream()
            .filter(n -> !typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism().keySet().contains(n.getNumber()))
            .forEach(numberDateSubject -> stringBuilder.append("%s %s %s: %s %n".formatted(
                        numberDateSubject.getDate().format(formatter),
                        numberDateSubject.getSubjectName(),
                        numberDateSubject.getTypeOfWork(),
                        numberDateSubject.getNumber()
                    )
                )
            );
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        } else {
            OptionalDouble average = numberDateSubjects.stream()
                .map(NumberDateSubject::getNumber)
                .filter(number -> number.matches("\\d+"))
                .mapToDouble(Double::parseDouble)
                .average();
            stringBuilder.append(DefaultMessages.AVERAGE);
            stringBuilder.append(average.orElseGet(() -> 0.0));
        }
        return stringBuilder.toString();
    }

    public String arrivedQuarterMarks(TelegramUser user, QuarterMarksDTO statement1) {
        Statement statement = Statement.valueOf(statement1.getQuarter());
        List<NumberSubject> numbers = journalService.getStatementNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement);
        return linesToString1(numbers);
    }

    private String linesToString1(List<NumberSubject> numberSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberSubjects
            .forEach(numberDateSubject -> stringBuilder.append("%s: %s %n".formatted(
                        numberDateSubject.getSubjectName(),
                        numberDateSubject.getNumber()
                    )
                )
            );
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        } else {
            OptionalDouble average = numberSubjects.stream()
                .map(NumberSubject::getNumber)
                .filter(number -> number.matches("\\d+"))
                .mapToDouble(Double::parseDouble)
                .average();
            stringBuilder.append(DefaultMessages.AVERAGE);
            stringBuilder.append(average.orElseGet(() -> 0.0));
        }
        return stringBuilder.toString();
    }
}
