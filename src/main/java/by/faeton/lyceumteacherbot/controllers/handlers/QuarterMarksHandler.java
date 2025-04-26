package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.NumberSubject;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.lyceum.Statement;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.QUARTER_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuarterMarksHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;


    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.QUARTER_MARKS))
                .orElse(false);
            return b || update.getMessage().getText().equals(QUARTER_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                .find(getChatId(update))
                .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.QUARTER_MARKS))
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

        optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                .chatId(chatId)
                .text(arrivedQuarterMarks(user, Statement.THREE_QUARTER))//todo
                .build()),
            () -> sendMessages.add(SendMessage.builder()
                .chatId(chatId)
                .text(NOT_AUTHORIZER)
                .build()));

        return sendMessages;
    }

    private String arrivedQuarterMarks(User user, Statement statement) {
        List<NumberSubject> numbers = journalService.getStatementNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement);
        return linesToString1(numbers);
    }

    private String linesToString1(List<NumberSubject> numberSubjects) {
        StringBuilder stringBuilder = new StringBuilder();
        numberSubjects
            .forEach(numberDateSubject -> stringBuilder.append(numberDateSubject.getSubjectName())
                .append(numberDateSubject.getNumber())
                .append(" ")
                .append("\n"));
        if (stringBuilder.isEmpty()) {
            stringBuilder.append(NOT_AVAILABLE);
        }
        return stringBuilder.toString();
    }
}
