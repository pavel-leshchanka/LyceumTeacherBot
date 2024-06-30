package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.model.User;
import by.faeton.lyceumteacherbot.repositories.SheetListener;
import by.faeton.lyceumteacherbot.utils.addressgenerator.UserCellAddressGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StudentService {
    private final SheetListener sheetListener;
    private final UserCellAddressGenerator userCellAddressGenerator;

    public Boolean isStudentLaboratoryNotebook(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        String field = userCellAddressGenerator.getNameOfCellUserLaboratoryNotebook(user);
        return !sheetListener.getCell(listOfGoogleSheet, field).isEmpty();
    }

    public Boolean isStudentTestNotebook(User user) {
        String listOfGoogleSheet = user.getClassParallel() + user.getClassLetter();
        String field = userCellAddressGenerator.getNameOfCellUserTestNotebook(user);
        return !sheetListener.getCell(listOfGoogleSheet, field).isEmpty();
    }
}