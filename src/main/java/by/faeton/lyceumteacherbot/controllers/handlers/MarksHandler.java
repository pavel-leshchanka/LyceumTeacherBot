package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.MarksDTO;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.lyceum.Statement;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;

@Slf4j
@Component
public class MarksHandler extends Handler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final UserRepository userRepository;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;

    public MarksHandler(DialogAttributesService dialogAttributesService, TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository, UserRepository userRepository, JournalService journalService, SchoolConfig schoolConfig) {
        super(dialogAttributesService);
        this.typeAndValueOfAbsenteeismRepository = typeAndValueOfAbsenteeismRepository;
        this.userRepository = userRepository;
        this.journalService = journalService;
        this.schoolConfig = schoolConfig;
    }

    @Override
    DialogType getType() {
        return DialogType.MARKS;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();

        if (update.hasMessage()) {
            MarksDTO quarterMarksDTO = new MarksDTO();
            Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
            List<Pair<String, String>> list = Stream.of(Statement.FIRST_QUARTER, Statement.SECOND_QUARTER, Statement.THREE_QUARTER, Statement.FOUR_QUARTER)
                .map(e -> {
                    quarterMarksDTO.setQuarter(e.name());
                    String s = generateJsonWithPrefix(quarterMarksDTO);
                    return new Pair<>(e.getStatementName(), s);
                })
                .toList();
            optionalUser.ifPresentOrElse(user -> {
                    sendMessages.add(KeyboardUtil.getKeyboard(chatId, "Четверть", list));
                },
                () -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(NOT_AUTHORIZER)
                    .build()));
        }
        if (update.hasCallbackQuery()) {
            MarksDTO dtoFromCallback = getDTOFromCallback(update, MarksDTO.class);

            userRepository.findByTelegramId(chatId).ifPresent(user -> {
                String s = arrivedMarks(user, dtoFromCallback);
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(s)
                    .build());
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(Statement.valueOf(dtoFromCallback.getQuarter()).getStatementName())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
            });
        }
        return sendMessages;
    }

    private String arrivedMarks(User user, MarksDTO dtoFromCallback) {
        Statement statement = Statement.valueOf(dtoFromCallback.getQuarter());
        List<NumberDateSubject> numbers = journalService.getNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement);
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