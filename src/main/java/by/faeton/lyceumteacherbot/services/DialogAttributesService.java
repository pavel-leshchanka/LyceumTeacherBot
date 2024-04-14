package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.repositories.DialogAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DialogAttributesService {
    private final DialogAttributeRepository dialogAttributeRepository;

    public Optional<DialogAttribute> find(Long chatId) {
        Optional<DialogAttribute> byId = dialogAttributeRepository.findById(chatId);
        return byId;
    }

    public void createDialog(DialogTypeStarted dialogTypeStarted, Long chatId) {
        DialogAttribute dialogAttribute = DialogAttribute.builder()
                .id(chatId)
                .dialogTypeStarted(dialogTypeStarted)
                .stepOfDialog(0)
                .receivedData(new ArrayList<>())
                .build();
        dialogAttributeRepository.save(dialogAttribute);
    }

    public void nextStep(DialogAttribute dialogAttribute, String receivedData) {
        dialogAttribute.getReceivedData().add(receivedData);
        dialogAttribute.setStepOfDialog(dialogAttribute.getStepOfDialog() + 1);
        dialogAttributeRepository.save(dialogAttribute);
    }

    public void finalStep(Long chatId) {
        dialogAttributeRepository.deleteById(chatId);
    }
}
