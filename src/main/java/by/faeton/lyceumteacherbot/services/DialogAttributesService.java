package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.DialogStarted;
import by.faeton.lyceumteacherbot.repositories.DialogAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DialogAttributesService {
    private final DialogAttributeRepository dialogAttributeRepository;

    public Optional<DialogAttribute> find(Long chatId){
        Optional<DialogAttribute> byId = dialogAttributeRepository.findById(chatId);
        return byId;
    }

    public void createDialog(DialogStarted dialogStarted, Long chatId){
        DialogAttribute dialogAttribute = new DialogAttribute();
        dialogAttribute.setId(chatId);
        dialogAttribute.setDialogStarted(dialogStarted);
        dialogAttribute.setStepOfDialog(0);
        dialogAttribute.setReceivedData(new ArrayList<>());
        dialogAttributeRepository.save(dialogAttribute);
    }

    public void nextStep(DialogAttribute dialogAttribute, ArrayList receivedData){
        dialogAttribute.setReceivedData(receivedData);
        dialogAttribute.setStepOfDialog(dialogAttribute.getStepOfDialog() + 1);
        dialogAttributeRepository.save(dialogAttribute);
    }
    public void finalStep(Long chatId){
        dialogAttributeRepository.deleteById(chatId);
    }
}
