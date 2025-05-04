package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.QuarterMarksDTO;
import by.faeton.lyceumteacherbot.model.Statement;
import by.faeton.lyceumteacherbot.security.TelegramUser;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
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
import java.util.Arrays;
import java.util.List;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.QUARTER;

@Slf4j
@Component
public class QuarterMarksHandler extends Handler {

    private final MarksService marksService;
    private final TelegramUserService userService;

    public QuarterMarksHandler(
        DialogAttributesService dialogAttributesService,
        MarksService marksService,
        TelegramUserService userService
    ) {
        super(dialogAttributesService);
        this.marksService = marksService;
        this.userService = userService;
    }

    @Override
    DialogType getType() {
        return DialogType.QUARTER_MARKS;
    }

    @Override
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<BotApiMethod> execute(Update update) {
        Long chatId = UpdateUtil.getChatId(update);
        List<BotApiMethod> sendMessages = new ArrayList<>();

        if (update.hasMessage()) {
            QuarterMarksDTO quarterMarksDTO = new QuarterMarksDTO();
            List<Pair<String, String>> list = Arrays.stream(Statement.values())
                .map(statement -> {
                    quarterMarksDTO.setQuarter(statement.name());
                    String jsonWithPrefix = generateJsonWithPrefix(quarterMarksDTO);
                    return new Pair<>(statement.getStatementName(), jsonWithPrefix);
                })
                .toList();
            sendMessages.add(KeyboardUtil.getKeyboard(chatId, QUARTER, list, 2));
        }
        if (update.hasCallbackQuery()) {
            QuarterMarksDTO dtoFromCallback = getDTOFromCallback(update, QuarterMarksDTO.class);
            sendMessages.add(EditMessageText.builder()
                .chatId(chatId)
                .text(Statement.valueOf(dtoFromCallback.getQuarter()).getStatementName())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build());
            TelegramUser user = userService.findByTelegramId(chatId);
            String marks = marksService.arrivedQuarterMarks(user, dtoFromCallback);
            sendMessages.add(SendMessage.builder()
                .chatId(chatId)
                .text(marks)
                .build());
            dialogAttributesService.delete(chatId);
        }
        return sendMessages;
    }
}
