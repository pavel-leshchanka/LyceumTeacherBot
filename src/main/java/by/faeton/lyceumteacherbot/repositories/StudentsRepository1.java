package by.faeton.lyceumteacherbot.repositories;


import by.faeton.lyceumteacherbot.config.FieldsNameConfig;
import by.faeton.lyceumteacherbot.config.SheetListNameConfig;
import by.faeton.lyceumteacherbot.model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StudentsRepository1 {

    private final SheetListNameConfig sheetListNameConfig;
    private final FieldsNameConfig fieldsNameConfig;
    private final SheetListener sheetListener;
    private final UserRepository userRepository;

    private final List<Student> studentsList = new ArrayList<>();


    public Optional<Student> findByNumber(String studentNumber, String classNumberAndLetter) {
        Optional<Student> returnedStudent = studentsList.stream()
                .filter(student -> student.getStudentNumber().equals(studentNumber))
                .filter(student -> student.getStudentClassNumberAndLetter().equals(classNumberAndLetter))
                .findFirst();
        if (returnedStudent.isEmpty()) {
            log.info("Student " + studentNumber + " not found");
            refreshContext();
            returnedStudent = studentsList.stream()
                    .filter(student -> student.getStudentNumber().equals(studentNumber))
                    .filter(student -> student.getStudentClassNumberAndLetter().equals(classNumberAndLetter))
                    .findFirst();
        }
        return returnedStudent;
    }

    public List<Student> getAllStudentsForClass(String classNumberAndLetter) {
        refreshContext();
        return studentsList;
    }

    public void refreshContext() {
        log.info("Called refresh context method");
        Set<String> studentClasses = userRepository.getStudentClasses();
        studentClasses.forEach(studentsClass -> {
            Optional<List<List<String>>> values = sheetListener.getSheetList(sheetListNameConfig.absenteeismList() + studentsClass,
                    fieldsNameConfig.studentsFields());
            List<Student> list = new ArrayList<>();
            if (values.isPresent()) {
                for (List<String> value : values.get()) {
                    if (value.size() > 1) {
                        Student student = Student.builder()
                                .studentNumber(value.get(0))
                                .studentName(value.get(1))
                                .studentClassNumberAndLetter(studentsClass)
                                .build();
                        list.add(student);
                    }
                }
            }
            studentsList.clear();
            studentsList.addAll(list);
        });
    }
}