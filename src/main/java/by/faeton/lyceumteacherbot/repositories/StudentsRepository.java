package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.utils.SheetListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class StudentsRepository {

    private final SheetListNameConfig sheetListNameConfig;
    private final FieldsNameConfig fieldsNameConfig;
    private final SheetListener sheetListener;

    private final List<Student> studentsList = new ArrayList<>();

    private static final Logger log = LoggerFactory.getLogger(StudentsRepository.class);

    public Optional<Student> findByNumber(String number) {
        Optional<Student> returnedStudent = studentsList.stream()
                .filter(student -> student.getStudentNumber().equals(number))
                .findFirst();
        if (returnedStudent.isEmpty()) {
            log.info("Student " + number + " not found");
            refreshContext();
            returnedStudent = studentsList.stream()
                    .filter(student -> student.getStudentNumber().equals(number))
                    .findFirst();
        }
        return returnedStudent;
    }

    public List<Student> getAllStudents() {
        refreshContext();
        return studentsList;
    }

    public void refreshContext() {
        log.info("Called refresh context method");
        Optional<ArrayList<ArrayList<String>>> values = sheetListener.getSheetList(sheetListNameConfig.absenteeismList(),
                fieldsNameConfig.studentsFields());
        List<Student> list = new ArrayList<>();
        if (values.isPresent()) {
            for (ArrayList<String> value : values.get()) {
                if (value.size() > 1) {
                    Student student = Student.builder()
                            .studentNumber(value.get(0))
                            .studentName(value.get(1))
                            .build();
                    list.add(student);
                }
            }
        }
        studentsList.clear();
        studentsList.addAll(list);
    }


}
