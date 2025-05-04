package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.AbsenteeismTextDTO;
import by.faeton.lyceumteacherbot.services.AbsenteeismService;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;

@Slf4j
@Component
public class AbsenteeismTextHandler extends Handler {
    private final AbsenteeismService absenteeismService;
    private final JournalService journalService;

    public AbsenteeismTextHandler(
        AbsenteeismService absenteeismService,
        DialogAttributesService dialogAttributesService,
        JournalService journalService
    ) {
        super(dialogAttributesService);
        this.absenteeismService = absenteeismService;
        this.journalService = journalService;
    }

    @Override
    DialogType getType() {
        return DialogType.ABSENTEEISM_TEXT;
    }

    @Override
    @PreAuthorize("hasAuthority('TEACHER')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            boolean isCommand = update.getMessage().getText().equals(DialogType.ABSENTEEISM_TEXT.getCommand());
            boolean dialogExist = dialogAttributesService.find(chatId) != null;
            if (isCommand && !dialogExist) {
                AbsenteeismTextDTO absenteeismTextDTO = new AbsenteeismTextDTO();
                Set<String> classParallels = journalService.getClassParallels();
                List<Pair<String, String>> pairList = classParallels.stream()
                    .map(parallel -> {
                        absenteeismTextDTO.setClassParallel(parallel);
                        String jsonWithPrefix = generateJsonWithPrefix(absenteeismTextDTO);
                        return new Pair<>(parallel, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_PARALLEL, pairList, 4));
            }
        }
        if (update.hasCallbackQuery()) {
            AbsenteeismTextDTO dtoFromCallback = getDTOFromCallback(update, AbsenteeismTextDTO.class);
            if (dtoFromCallback.getClassLetter() == null) {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(dtoFromCallback.getClassParallel())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                Set<String> classLetters = journalService.getClassLetters();
                List<Pair<String, String>> list = classLetters.stream()
                    .map(letter -> {
                        dtoFromCallback.setClassLetter(letter);
                        String jsonWithPrefix = generateJsonWithPrefix(dtoFromCallback);
                        return new Pair<>(letter, jsonWithPrefix);
                    })
                    .toList();
                sendMessages.add(KeyboardUtil.getKeyboard(chatId, CLASS_LETTER, list, 4));
            } else {
                sendMessages.add(EditMessageText.builder()
                    .chatId(chatId)
                    .text(dtoFromCallback.getClassLetter())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build());
                sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(absenteeismService.getTextOfAbsenteeismOnCurrentDate(LocalDate.now(), dtoFromCallback))
                    .build());
            }
        }
        return sendMessages;
    }
}
