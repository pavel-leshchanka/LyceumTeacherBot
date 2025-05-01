package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.QuarterMarksDTO;
import by.faeton.lyceumteacherbot.model.DTO.NumberSubject;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.model.lyceum.Statement;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;

@Slf4j
@Component
public class QuarterMarksHandler extends Handler {

    private final UserRepository userRepository;
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;

    public QuarterMarksHandler(DialogAttributesService dialogAttributesService, UserRepository userRepository, JournalService journalService, SchoolConfig schoolConfig) {
        super(dialogAttributesService);
        this.userRepository = userRepository;
        this.journalService = journalService;
        this.schoolConfig = schoolConfig;
    }

    @Override
    DialogType getType() {
        return DialogType.QUARTER_MARKS;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();

        if (update.hasMessage()) {
            QuarterMarksDTO quarterMarksDTO = new QuarterMarksDTO();
            Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
            List<Pair<String, String>> list = Arrays.stream(Statement.values())
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
            QuarterMarksDTO dtoFromCallback = getDTOFromCallback(update, QuarterMarksDTO.class);

            userRepository.findByTelegramId(chatId).ifPresent(user -> {
                String s = arrivedQuarterMarks(user, dtoFromCallback);
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
            dialogAttributesService.remove(chatId);
        }
        return sendMessages;
    }

    private String arrivedQuarterMarks(User user, QuarterMarksDTO statement1) {
        Statement statement = Statement.valueOf(statement1.getQuarter());
        List<NumberSubject> numbers = journalService.getStatementNumbers(user.getSubjectOfEducationId(), schoolConfig.currentAcademicYear(), statement);
        return linesToString(numbers);
    }

    private String linesToString(List<NumberSubject> numberSubjects) {
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
