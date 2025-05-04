package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.MarksDTO;
import by.faeton.lyceumteacherbot.model.Statement;
import by.faeton.lyceumteacherbot.model.Subject;
import by.faeton.lyceumteacherbot.security.TelegramUser;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.services.MarksService;
import by.faeton.lyceumteacherbot.services.TelegramUserService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.QUARTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.SUBJECT;

@Slf4j
@Component
public class MarksHandler extends Handler {
    private final SchoolConfig schoolConfig;
    private final JournalService journalService;
    private final MarksService marksService;
    private final TelegramUserService userService;

    public MarksHandler(
        DialogAttributesService dialogAttributesService,
        MarksService marksService,
        TelegramUserService userService,
        JournalService journalService,
        SchoolConfig schoolConfig
    ) {
        super(dialogAttributesService);
        this.marksService = marksService;
        this.userService = userService;
        this.journalService = journalService;
        this.schoolConfig = schoolConfig;
    }

    @Override
    DialogType getType() {
        return DialogType.MARKS;
    }

    @Override
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();

        if (update.hasMessage()) {
            MarksDTO marksDTO = new MarksDTO();
            List<Pair<String, String>> list = Stream.of(
                    Statement.FIRST_QUARTER,
                    Statement.SECOND_QUARTER,
                    Statement.THREE_QUARTER,
                    Statement.FOUR_QUARTER
                )
                .map(statement -> {
                    marksDTO.setQuarter(statement.name());
                    String jsonWithPrefix = generateJsonWithPrefix(marksDTO);
                    return new Pair<>(statement.getStatementName(), jsonWithPrefix);
                })
                .toList();
            sendMessages.add(KeyboardUtil.getKeyboard(chatId, QUARTER, list, 2));
        }
        if (update.hasCallbackQuery()) {
            MarksDTO marksDTO = getDTOFromCallback(update, MarksDTO.class);
            if (marksDTO.getSubject() == null) {
                Statement statement = Statement.valueOf(marksDTO.getQuarter());
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(statement.getStatementName())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                TelegramUser byTelegramId = userService.findByTelegramId(chatId);
                List<Subject> subjects = journalService.getSubjects(byTelegramId.getSubjectOfEducationId(), schoolConfig.currentAcademicYear());
                List<Pair<String, String>> list = subjects.stream()
                    .map(subject -> {
                            marksDTO.setSubject(subject.getId().toString());
                            String jsonWithPrefix = generateJsonWithPrefix(marksDTO);
                            return new Pair<>(subject.getName(), jsonWithPrefix);
                        }
                    )
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, SUBJECT, list, 4));
            } else {
                TelegramUser byTelegramId = userService.findByTelegramId(chatId);
                List<Subject> subjects = journalService.getSubjects(byTelegramId.getSubjectOfEducationId(), schoolConfig.currentAcademicYear());
                long subjectId = Long.parseLong(marksDTO.getSubject());
                Subject subject = subjects.stream()
                    .filter(sub -> sub.getId().equals(subjectId))
                    .findFirst()
                    .orElseThrow();
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(subject.getName())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                TelegramUser user = userService.findByTelegramId(chatId);
                String marks = marksService.arrivedMarks(user, marksDTO);
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(marks)
                    .build());
            }
        }
        return sendMessages;
    }
}