package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DialogAttributesService {

    private final Map<Long, CommandHandler> commandHandlerMap = new HashMap<>();

    public boolean exist(Long chatId, DialogType type) {
        CommandHandler commandHandler = commandHandlerMap.get(chatId);
        return commandHandler != null && commandHandler.getType().equals(type);
    }

    public void save(Long chatId, CommandHandler handler) {
        commandHandlerMap.put(chatId, handler);
    }

    public CommandHandler find(Long chatId) {
        return commandHandlerMap.get(chatId);
    }

    public void delete(Long chatId) {
        commandHandlerMap.remove(chatId);
    }
}