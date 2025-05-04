package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.CommandHandler;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.utils.UpdateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@AllArgsConstructor
public abstract class Handler {
    final DialogAttributesService dialogAttributesService;
    private final ObjectMapper mapper = new ObjectMapper();

    public abstract List<BotApiMethod> execute(Update update);

    abstract DialogType getType();

    public boolean isAppropriateTypeMessage(Update update) {
        DialogType type = getType();
        boolean isAppropriateCommand = update.hasMessage() && update.getMessage().getText().equals(type.getCommand());
        boolean isAppropriateCallback = update.hasCallbackQuery() && update.getCallbackQuery().getData().substring(0, 3).equals(type.getPrefix());
        boolean isCancel = update.hasCallbackQuery() && update.getCallbackQuery().getData().equals(CancelHandler.getCancelCallback());
        boolean isDialogStarted = dialogAttributesService.exist(UpdateUtil.getChatId(update), getType());
        return isAppropriateCommand || isAppropriateCallback || (isDialogStarted && !isCancel);
    }

    public <T> T getDTOFromCallback(Update update, Class<T> clazz) {
        try {
            T absenteeismDTO;
            String substring = update.getCallbackQuery().getData().substring(3);
            absenteeismDTO = mapper.readValue(substring, clazz);
            return absenteeismDTO;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateJsonWithPrefix(CommandHandler absenteeismDTO) {
        try {
            String ss = mapper.writeValueAsString(absenteeismDTO);
            return getType().getPrefix() + ss;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
