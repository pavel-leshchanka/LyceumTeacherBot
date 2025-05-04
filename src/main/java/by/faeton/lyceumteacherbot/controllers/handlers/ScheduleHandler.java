package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.model.ScheduleDays;
import by.faeton.lyceumteacherbot.model.Semester;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.ScheduleDTO;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import by.faeton.lyceumteacherbot.services.ScheduleService;
import by.faeton.lyceumteacherbot.utils.KeyboardUtil;
import by.faeton.lyceumteacherbot.utils.Pair;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.DAYS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.SEMESTR;

@Component
public class ScheduleHandler extends Handler {
    private final JournalService journalService;
    private final ScheduleService scheduleService;

    public ScheduleHandler(
        DialogAttributesService dialogAttributesService,
        JournalService journalService,
        ScheduleService scheduleService
    ) {
        super(dialogAttributesService);
        this.journalService = journalService;
        this.scheduleService = scheduleService;
    }

    @Override
    DialogType getType() {
        return DialogType.SCHEDULE;
    }

    @Override
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            boolean isCommand = update.getMessage().getText().equals(DialogType.SCHEDULE.getCommand());
            boolean dialogExist = dialogAttributesService.find(chatId) != null;
            if (isCommand && !dialogExist) {
                ScheduleDTO scheduleDTO = new ScheduleDTO();
                Set<String> classParallels = journalService.getClassParallels();
                List<Pair<String, String>> pairList = classParallels.stream()
                    .map(parallel -> {
                        scheduleDTO.setClassParallel(parallel);
                        String jsonWithPrefix = generateJsonWithPrefix(scheduleDTO);
                        return new Pair<>(parallel, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_PARALLEL, pairList, 4));
            }
        }
        if (update.hasCallbackQuery()) {
            ScheduleDTO dtoFromCallback = getDTOFromCallback(update, ScheduleDTO.class);
            if (dtoFromCallback.getClassLetter() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(dtoFromCallback.getClassParallel())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> classLetters = journalService.getClassLetters();
                List<Pair<String, String>> pairList = classLetters.stream()
                    .map(letter -> {
                        dtoFromCallback.setClassLetter(letter);
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(letter, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_LETTER, pairList, 4));
            } else if (dtoFromCallback.getSemester() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(dtoFromCallback.getClassLetter())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> pairList = Arrays.stream(Semester.values())
                    .map(semester -> {
                        dtoFromCallback.setSemester(semester.name());
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(semester.getSemesterName(), jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, SEMESTR, pairList, 2));
            } else if (dtoFromCallback.getDay() == null) {
                Semester semester = Semester.valueOf(dtoFromCallback.getSemester());
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(semester.getSemesterName())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                List<Pair<String, String>> pairList = Arrays.stream(ScheduleDays.values())
                    .map(letter -> {
                        dtoFromCallback.setDay(letter.name());
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(letter.getDayName(), jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, DAYS, pairList, 4));
            } else {
                ScheduleDays days = ScheduleDays.valueOf(dtoFromCallback.getDay());
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(days.getDayName())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                String text = scheduleService.getText(dtoFromCallback);
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
            }
        }
        return sendMessages;
    }
}
