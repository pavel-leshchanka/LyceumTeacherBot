package by.faeton.lyceumteacherbot.services;

import by.faeton.lyceumteacherbot.config.SchoolConfig;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.AbsenteeismDTO;
import by.faeton.lyceumteacherbot.controllers.handlers.dto.AbsenteeismTextDTO;
import by.faeton.lyceumteacherbot.model.Student;
import by.faeton.lyceumteacherbot.model.dto.NumberDateNumberOfSubject;
import by.faeton.lyceumteacherbot.model.dto.StudentWithNumberAndNumberOfTask;
import by.faeton.lyceumteacherbot.repositories.StudentsRepository;
import by.faeton.lyceumteacherbot.repositories.TypeAndValueOfAbsenteeismRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static by.faeton.lyceumteacherbot.utils.DefaultMessages.ABSENTEEISM_NOT_FOUND;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.DASH;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.POINT;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASK;
import static by.faeton.lyceumteacherbot.utils.DefaultMessages.TASKS;

@Service
@RequiredArgsConstructor
public class AbsenteeismService {
    private final JournalService journalService;
    private final SchoolConfig schoolConfig;
    private final StudentsRepository studentsRepository;
    private final TypeAndValueOfAbsenteeismRepository typeAndValueOfAbsenteeismRepository;

    public boolean writeAbsenteeism(AbsenteeismDTO dialogAttribute) {
        int startOfAbsenteeism = Integer.parseInt(dialogAttribute.getStartOfAbsenteeism());
        int endOfAbsenteeism = Integer.parseInt(dialogAttribute.getEndOfAbsenteeism());

        Optional<Student> optionalStudent = studentsRepository.findByStudentId(dialogAttribute.getStudentId());
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            List<NumberDateNumberOfSubject> list = new ArrayList<>();
            for (int i = startOfAbsenteeism; i <= endOfAbsenteeism; i++) {
                list.add(new NumberDateNumberOfSubject(
                    dialogAttribute.getTypeOfAbsenteeism(), LocalDate.now(), i
                ));
            }
            journalService.writeAbsenteeism(list, student, dialogAttribute.getClassParallel(), dialogAttribute.getClassLetter(), schoolConfig.currentAcademicYear());
            return true;
        }

        return false;
    }


    public String getTextOfAbsenteeismOnCurrentDate(LocalDate localDate, AbsenteeismTextDTO absenteeismTextDTO) {
        String classParallel = absenteeismTextDTO.getClassParallel();
        String classLetter = absenteeismTextDTO.getClassLetter();
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
