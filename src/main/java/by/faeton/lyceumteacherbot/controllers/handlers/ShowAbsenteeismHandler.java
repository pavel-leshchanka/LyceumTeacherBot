package by.faeton.lyceumteacherbot.controllers.handlers;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.model.DTO.StudentWithNumberAndNumberOfTask;
import by.faeton.lyceumteacherbot.model.DialogTypeStarted;
import by.faeton.lyceumteacherbot.model.UserLevel;
import by.faeton.lyceumteacherbot.repositories.JournalRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import by.faeton.lyceumteacherbot.repositories.UserRepository;
import by.faeton.lyceumteacherbot.services.DialogAttributesService;
import by.faeton.lyceumteacherbot.services.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.utils.CallbackQueryStatic.CANCEL_CALLBACK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ABSENTEEISM_NOT_FOUND;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CANCEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_LETTER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.CLASS_PARALLEL;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.DASH;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NOT_AUTHORIZER;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.NO_ACCESS;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.POINT;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASKS;
import static by.faeton.lyceumteacherbot.utils.TelegramCommand.ABSENTEEISM_TEXT_COMMAND;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShowAbsenteeismHandler implements Handler {


    private final DialogAttributesService dialogAttributesService;
    private final JournalService journalService;
    private final UserRepository userRepository;
    private final JournalRepository journalRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;
    private final SchoolConfig schoolConfig;

    @Override
    public boolean isAppropriateTypeMessage(Update update) {
        if (update.hasMessage()) {
            Boolean b = dialogAttributesService
                    .find(update.getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SHOW_ABSENTEEISM))
                    .orElse(false);
            return b || update.getMessage().getText().equals(ABSENTEEISM_TEXT_COMMAND);
        }
        if (update.hasCallbackQuery()) {
            return dialogAttributesService
                    .find(update.getCallbackQuery()
                            .getMessage()
                            .getChatId())
                    .map(dialogAttribute -> dialogAttribute.getDialogTypeStarted().equals(DialogTypeStarted.SHOW_ABSENTEEISM))
                    .orElse(false);
        }
        return false;
    }

    @Override
    public List<BotApiMethod> execute(Update update) {
        List sendMessages = new ArrayList<>();
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().getText().equals(ABSENTEEISM_TEXT_COMMAND) && dialogAttributesService.find(chatId).isEmpty()) {
                userRepository.findByTelegramId(chatId).ifPresentOrElse(user -> {
                            if (user.getUserLevel().ordinal() >= UserLevel.TEACHER.ordinal()) {
                                Set<String> classParallels = journalRepository.getClassParallels();
                                sendMessages.add(getKeyboard(chatId,
                                        CLASS_PARALLEL,
                                        classParallels
                                ));
                                dialogAttributesService.createDialog(DialogTypeStarted.SHOW_ABSENTEEISM, chatId);
                            } else {
                                sendMessages.add(SendMessage.builder()
                                        .chatId(chatId)
                                        .text(NO_ACCESS)
                                        .build());
                            }
                        },
                        () -> sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(NOT_AUTHORIZER)
                                .build()));
            }
        }


        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            dialogAttributesService.find(chatId).ifPresent(dialogAttribute -> {
                switch (dialogAttribute.getStepOfDialog()) {
                    case 0 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());
                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());
                        Set<String> classLetters = journalRepository.getClassLetters();
                        sendMessages.add(getKeyboard(chatId,
                                CLASS_LETTER,
                                classLetters
                        ));
                    }
                    case 1 -> {
                        sendMessages.add(EditMessageText.builder()
                                .chatId(chatId)
                                .text(update.getCallbackQuery().getData())
                                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                .build());

                        dialogAttributesService.nextStep(dialogAttribute, update.getCallbackQuery().getData());

                        String classParallel = dialogAttribute.getReceivedData().get(0);
                        String classLetter = dialogAttribute.getReceivedData().get(1);

                        sendMessages.add(SendMessage.builder()
                                .chatId(chatId)
                                .text(getTextOfAbsenteeismOnCurrentDate(LocalDate.now(), classLetter, classParallel))
                                .build());
                        dialogAttributesService.deleteByTelegramId(chatId);

                    }
                }
            });
        }
        return sendMessages;
    }

    private SendMessage getKeyboard(Long chatId, String text, Set<String> callbackData) {
        List<InlineKeyboardRow> rowsInline = new ArrayList<>();
        for (String s : callbackData) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(s)
                    .callbackData(s)
                    .build());
            rowsInline.add(new InlineKeyboardRow(row));
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(CANCEL_CALLBACK)
                .build());
        rowsInline.add(new InlineKeyboardRow(row));
        InlineKeyboardMarkup markupInline = InlineKeyboardMarkup.builder()
                .keyboard(rowsInline)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(markupInline)
                .text(text)
                .build();
    }


    private String getTextOfAbsenteeismOnCurrentDate(LocalDate localDate, String classLetter, String classParallel) {
        List<StudentWithNumberAndNumberOfTask> studentsAbsenteeism = journalService.getStudentsAbsenteeism(localDate, classLetter, classParallel, schoolConfig.currentAcademicYear());
        Map<String, List<StudentWithNumberAndNumberOfTask>> collect = studentsAbsenteeism.stream()
                .collect(Collectors.groupingBy(StudentWithNumberAndNumberOfTask::getStudentName, Collectors.toList()));
        String s = classParallel + classLetter;
        if (!collect.isEmpty()) {
            for (List<StudentWithNumberAndNumberOfTask> studentWithNumberAndNumberOfTasks : collect.values()) {
                s += "\n" + studentWithNumberAndNumberOfTasks.get(0).getStudentName() + " ";
                studentWithNumberAndNumberOfTasks.sort(Comparator.comparing(StudentWithNumberAndNumberOfTask::getNumberOfTask));
                String s1 = studentWithNumberAndNumberOfTasks.get(0).getNumber();
                int start = studentWithNumberAndNumberOfTasks.get(0).getNumberOfTask();
                int current = start;
                int end;
                for (int j = 1; j < studentWithNumberAndNumberOfTasks.size(); j++) {
                    if (!studentWithNumberAndNumberOfTasks.get(j).getNumber().equals(s1) || !studentWithNumberAndNumberOfTasks.get(j).getNumberOfTask().equals(current + 1)) {
                        end = studentWithNumberAndNumberOfTasks.get(j - 1).getNumberOfTask();
                        s += generateTextAbsenteeismLine(start, end, s1);
                        s1 = studentWithNumberAndNumberOfTasks.get(j).getNumber();
                        start = studentWithNumberAndNumberOfTasks.get(j).getNumberOfTask();
                        current = start;
                    } else {
                        current++;
                    }
                }
                s += generateTextAbsenteeismLine(start, studentWithNumberAndNumberOfTasks.get(studentWithNumberAndNumberOfTasks.size() - 1).getNumberOfTask(), s1);
            }
        } else {
            s += "\n" + ABSENTEEISM_NOT_FOUND;
        }
        return s;
    }

    private String generateTextAbsenteeismLine(Integer start, Integer end, String type) {
        String ret = "";
        if (!type.isEmpty()) {
            if (start.equals(end)) {
                ret += start + TASK + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + POINT;
            }
            if (!start.equals(end)) {
                ret += start + DASH + end + TASKS + typeAndValueOfAbsenteeismRepository.getValueOfAbsenteeism(type) + POINT;
            }
        }
        return ret;
    }
}

