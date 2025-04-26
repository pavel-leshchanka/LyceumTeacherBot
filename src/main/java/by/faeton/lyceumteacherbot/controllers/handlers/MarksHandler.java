package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.MARKS_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class MarksHandler implements Handler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.MARKS))
                .orElse(false);
            return b || update.getMessage().getText().equals(MARKS_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.MARKS))
                .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
        switch (message.getText()) {
            case MARKS_COMMAND -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedMarks(user))
                    .build()),
                () -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(NOT_AUTHORIZER)
                    .build()));

        }
        return sendMessages;
    }

    private String arrivedMarks(User user) {
        List<NumberDateSubject> numbers = journalService.getNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear());
        return linesToString(numbers);
    }

    private String linesToString(List<NumberDateSubject> numberDateSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberDateSubjects
            .stream()
            .filter(n -> !typeAndValueOfAbsenteeismRepository.getAllTypeAndValueOfAbsenteeism().keySet().contains(n.getNumber()))
            .forEach(numberDateSubject -> stringBuilder
                .append(numberDateSubject.getDate().format(formatter))
                .append(" ")
                .append(numberDateSubject.getSubjectName())
                .append(" ")
                .append(numberDateSubject.getTypeOfWork())
                .append(" ")
                .append(numberDateSubject.getNumber())
                .append("\n"));
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        } else {
            OptionalDouble average = numberDateSubjects.stream()
                .map(NumberDateSubject::getNumber)
                .filter(number -> number.matches("\\d+"))
                .mapToDouble(Double::parseDouble)
                .average();
            stringBuilder.append("Среднее: ");
            stringBuilder.append(average.orElseGet(() -> 0.0));
        }
        return stringBuilder.toString();
    }


}