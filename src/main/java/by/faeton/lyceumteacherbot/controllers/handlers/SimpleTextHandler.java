package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.model.DialogAttribute;
import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.StudentService;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import by.faeton.lyceumteacherbot.utils.addressgenerator.UserCellAddressGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.AVAILABLE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.HELP;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AVAILABLE;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.START;

@Slf4j
@RequiredArgsConstructor
@Component
public class SimpleTextHandler implements Handler {

    private final DialogAttributesService dialogAttributesService;
    private final UserRepository userRepository;
    private final SheetListener sheetListener;
    private final UserCellAddressGenerator userCellAddressGenerator;
    private final StudentService studentService;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Optional<DialogAttribute> byId = dialogAttributesService.find(update.getMessage().getChatId());
            return update.getMessage().hasText() && byId.isEmpty();
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List<BotApiMethod> sendMessages = new ArrayList<>();
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        Optional<User> optionalUser = userRepository.findByTelegramId(chatId);
        switch (message.getText()) {
            case "/start" -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedStart())
                    .build());
            case "/marks" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedMarks(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/quarter" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedQuarterMarks(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/laboratory_notebook" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedLaboratoryNotebook(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/test_notebook" -> optionalUser.ifPresentOrElse(user -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(arrivedTestNotebook(user))
                            .build()),
                    () -> sendMessages.add(SendMessage.builder()
                            .chatId(chatId)
                            .text(NOT_AUTHORIZER)
                            .build()));
            case "/help" -> sendMessages.add(SendMessage.builder()
                    .chatId(chatId)
                    .text(arrivedHelp())
                    .build());
        }
        return sendMessages;
    }

    private String arrivedStart() {
        return START;
    }

    private String arrivedMarks(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetListFromCache(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfDate());
        Optional<List<List<String>>> sheetTypeLine = sheetListener.getSheetListFromCache(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfTypeOfWork());
        Optional<List<List<String>>> sheetMarksLine = sheetListener.getSheetList(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfMarks(user));
        String sheetText = linesToString(sheetDateLine, sheetTypeLine, sheetMarksLine);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedQuarterMarks(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        Optional<List<List<String>>> sheetDateLine = sheetListener.getSheetListFromCache(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfQuarterName());
        Optional<List<List<String>>> sheetTypeLine = sheetListener.getSheetListFromCache(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfTypeOfQuarter());
        Optional<List<List<String>>> sheetQuarterLine = sheetListener.getSheetList(listOfGoogleSheet, userCellAddressGenerator.getCellsNameOfQuarterMarks(user));
        String sheetText = linesToString(sheetDateLine, sheetTypeLine, sheetQuarterLine);
        if (sheetText.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return sheetText;
    }

    private String arrivedLaboratoryNotebook(User user) {
        if (studentService.isStudentLaboratoryNotebook(user)) {
            return AVAILABLE;
        }
        return NOT_AVAILABLE;
    }

    private String arrivedTestNotebook(User user) {
        if (studentService.isStudentTestNotebook(user)) {
            return AVAILABLE;
        }
        return NOT_AVAILABLE;
    }

    private String arrivedHelp() {
        return HELP;
    }

    @SafeVarargs
    private final String linesToString(Optional<List<List<String>>>... values) {
        List<List<String>> returnedList = new ArrayList<>();
        for (Optional<List<List<String>>> firstValue : values) {
            firstValue.ifPresent(arrayLists -> returnedList.add(arrayLists.getFirst()));
        }
        String returnedText = "";
        if (returnedList.size() > 1) {
            int lastNumberListOfValues = returnedList.size() - 1;
            List<String> lastList = returnedList.get(lastNumberListOfValues);
            for (int i = 0; i < lastList.size(); i++) {
                if (lastList.get(i) != null && !lastList.get(i).isEmpty()) {
                    for (List<String> strings : returnedList) {
                        if (i < strings.size()) {
                            returnedText += strings.get(i) + " ";
                        }
                    }
                    returnedText += '\n';
                }
            }
        } else {
            returnedText = returnedText + NOT_AVAILABLE;
        }
        return returnedText;
    }
}