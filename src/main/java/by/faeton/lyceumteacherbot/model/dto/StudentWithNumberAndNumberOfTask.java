package by.faeton.lyceumteacherbot.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StudentWithNumberAndNumberOfTask {
    private String studentName;
    private String number;
    private Integer numberOfTask;
}
