package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.controllers.DialogType;
import by.faeton.lyceumteacherbot.controllers.handlers.DTO.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DialogAttributesService {

    private final Map<Long, CommandHandler> handlerMap = new HashMap<>();

    public boolean supported(Long chatiD, DialogType type) {
        CommandHandler commandHandler = handlerMap.get(chatiD);
        return commandHandler != null && commandHandler.getType().equals(type);
    }

    public void save(Long chatid, CommandHandler handler) {
        handlerMap.put(chatid, handler);
    }

    public CommandHandler get(Long chatid) {
        return handlerMap.get(chatid);
    }

    public void remove(Long chatid) {
        handlerMap.remove(chatid);
    }
}