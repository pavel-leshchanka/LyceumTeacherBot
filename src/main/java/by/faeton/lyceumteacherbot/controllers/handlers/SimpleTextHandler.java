package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.NumberDateSubject;
import by.faeton.lyceumteacherbot.model.DTO.NumberSubject;
import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.model.lyceum.Statement;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.BotService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.HELP;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.REFRESHED;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.HELP_COMMAND;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.MARKS_COMMAND;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.QUARTER_COMMAND;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.REFRESH_COMMAND;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.START_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class SimpleTextHandler implements Handler {


    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final JournalService journalService;
    private final BotService botService;
    private final SchoolConfig schoolConfig;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Optional<DialogAttribute> byId = dialogAttributesService.find(update.getMessage().getChatId());
            return update.getMessage().hasText() && byId.isEmpty();
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
            case START_COMMAND -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedStart())
                    .build());
            case MARKS_COMMAND -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedMarks(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case QUARTER_COMMAND -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedQuarterMarks(user, Statement.FIRST_QUARTER))//todo
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case REFRESH_COMMAND -> optionalUser.ifPresentOrElse(user -> {
                        if (user.getUserLevel().equals(UserLevel.ADMIN)) {
                            botService.refreshContext();
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(REFRESHED)
                                    .build());
                        } else {
                            sendMessages.add(SendMessage.builder()
                                    .chatId(chatId)
                                    .text(NO_ACCESS)
                                    .build());
                        }
                    },
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case HELP_COMMAND -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedHelp())
                    .build());
        }
        return sendMessages;
    }

    private String arrivedStart() {
        return START;
    }

    private String arrivedMarks(User user) {
        List<NumberDateSubject> numbers = journalService.getNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear());
        return linesToString(numbers);
    }

    private String arrivedQuarterMarks(User user, Statement statement) {
        List<NumberSubject> numbers = journalService.getStatementNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement);
        return linesToString1(numbers);
    }

    private String arrivedHelp() {
        return HELP;
    }


    private String linesToString(List<NumberDateSubject> numberDateSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberDateSubjects
                .forEach(numberDateSubject -> stringBuilder.append(numberDateSubject.getDate())
                        .append(numberDateSubject.getSubjectName())
                        .append(numberDateSubject.getNumber())
                        .append("\n"));
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        }
        return stringBuilder.toString();
    }

    private String linesToString1(List<NumberSubject> numberSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberSubjects
                .forEach(numberDateSubject -> stringBuilder.append(numberDateSubject.getSubjectName())
                        .append(numberDateSubject.getNumber())
                        .append("\n"));
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        }
        return stringBuilder.toString();
    }
}